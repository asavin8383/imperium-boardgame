package services;

import analysis.AnalysisResult;
import analysis.CheckUnitResult;
import analysis.CheckUnitStatusNotification;
import checkUnits.CheckUnitKey;
import common.DispatcherProperties;
import enums.CheckUnitJobResult;
import exceptions.AS_15_8_DispatcherException;
import imprint.HeaderObject;
import imprint.ImageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.enums.ArrangementStatus;
import model.enums.CheckType;
import org.apache.kafka.streams.KeyValue;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import repositories.ArrangementRepo;
import repositories.ResultRepo;
import repositories.ResultScreenShotRepo;
import restapi.ArrangementRestApi;
import restapi.ConfigClient;
import restapi.PptClient;

import javax.annotation.PostConstruct;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ResultService {

    private final ResultsKafkaService resultsKafkaService;

    private final ArrangementRepo arrangementRepo;
    private final ResultRepo resultRepo;
    private final ResultScreenShotRepo resultScreenShotRepo;
    private final ArrangementRestApi arrangementRestApi;
    private final ArrangementService arrangementService;
    //Объекты для создания штампа на скриншоте
    private final ImageProcessor imageProcessor = new ImageProcessor();
    private final HeaderObject headerObject = new HeaderObject();
    private final DispatcherProperties dispatcherProperties;
    private final ConfigClient configClient;
    private final PptClient pptClient;

    @PostConstruct
    private void initImageProcessor() throws Exception {
        //Загружаем шрифт
        imageProcessor.loadFontFromFile(Objects.requireNonNull(ResultService.class.getClassLoader().getResourceAsStream("fonts/arial.ttf")));
    }

    @Scheduled(cron = "${results.save.schedule}")
    public void saveCompletionArrangements() {
        try{
            arrangementRepo
                    .findReadyToUpload()
                    .stream()
                    .filter(arrangement -> {
                        if(arrangement.getStatus().equals(ArrangementStatus.STOPPING) || isArrangementFinished(arrangement)) {
                            arrangement.setStatus(ArrangementStatus.UPLOADING);
                            arrangementRepo.save(arrangement);
                            return true;
                        }
                        return false;
                    })
                    .forEach(this::saveArrangementResults);
        } catch (Exception ex){
            log.error("Ошибка при сохранении результатов мероприятий", ex);
        }
    }

    public void saveArrangementResults(Long arrangementId){
        Arrangement arrangement = arrangementRepo
                .findById(arrangementId)
                .orElseThrow((() -> new AS_15_8_DispatcherException("Ошибка! Мероприятие для выгрузки не найдено в БД по id: " + arrangementId)));
        arrangement.setStatus(ArrangementStatus.UPLOADING);
        arrangementRepo.save(arrangement);
        saveArrangementResults(arrangement);
    }

    private void saveArrangementResults(Arrangement arrangement) {
        log.info("Начато сохранение мероприятия: " + arrangement.getId());
        boolean isStopped = !arrangementService.isArrangementRunning(arrangement.getId(), arrangement.getVersion());
        try {
            resultsKafkaService.getArrangementResultsIterator(arrangement.getId())
                .ifPresent(resultsIterator -> {
                    boolean isSaved = true;
                    while (resultsIterator.hasNext()) {
                        KeyValue<CheckUnitKey, CheckUnitResult> result = resultsIterator.next();
                        isSaved = saveArrangementResult(result.key, result.value, arrangement, getAccessToolInfo(arrangement.getId()));
                    }
                    if (isSaved) {
                        log.info("Мероприятие успешно сохранено в БД: " + arrangement.getId());
                        if(isArrangementFinished(arrangement) || isStopped) {
                            if (arrangementRestApi.sendStatusNotificationToPPM(arrangement.getId(), isStopped)) {
                                boolean isActAvailable = arrangementRestApi.isActAvailableFromPPT(arrangement.getId());
                                boolean isFinished = arrangementService.finishArrangement(arrangement.getId(), isStopped, isActAvailable);
                                if (!isStopped && isFinished && isActAvailable)
                                    arrangementRestApi.changeArrangementStatusToActSentPPT(arrangement.getId());
                                log.info("Мероприятие успешно завершено: " + arrangement.getId());
                            }
                        }
                    } else {
                        log.info("Ошибка сохранения мероприятия: " + arrangement.getId());
                    }
                });
        } catch (Exception ex){
            log.error("Ошибка при созранении результатов проверок мероприятия " + arrangement.getId(), ex);
            arrangement.setStatus(isStopped ? ArrangementStatus.STOPPING : ArrangementStatus.RUNNING);
            arrangementRepo.save(arrangement);
        }
    }

    private boolean saveArrangementResult(CheckUnitKey checkUnitKey, CheckUnitResult checkUnitResult, Arrangement arrangement, Map<String, String> accessToolInfo) {
        try {
            saveJobResult(arrangement, checkUnitKey.getJobId(), checkUnitResult, accessToolInfo);
        } catch (Exception ex) {
            try {
                log.error("Ошибка при сохранении результата проверки: " + checkUnitKey.getJobId() + ", " + checkUnitResult.getCheckUnit().getValue(), ex);

                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));

                CheckUnitStatusNotification notification = new CheckUnitStatusNotification();
                notification.setCheckResult(CheckUnitJobResult.INTERNAL_ERROR);
                notification.setCheckUnit(checkUnitResult.getCheckUnit());
                notification.setStartTime(checkUnitResult.getStartTime());
                notification.setEndTime(checkUnitResult.getEndTime());
                notification.setDescription(sw.toString());

                saveJobResult(arrangement, checkUnitKey.getJobId(), notification, accessToolInfo);
                log.info("Ошибка сохранена: " + checkUnitKey.getJobId());
            } catch (Exception newEx) {
                log.error("Ошибка при сохранении ошибочной обработки сообщения с анализом результатов проверки: " + checkUnitKey.getJobId() + ", " + checkUnitResult.getCheckUnit().getValue(), newEx);
                return false;
            }
        }
        return true;
    }

    private void saveJobResult(Arrangement arrangement, Long jobId, CheckUnitResult checkUnitResult, Map<String, String> accessToolInfo) {
        DetailResultService<? super CheckUnitResult, ? extends DetailResult> service = AnalysisResultServiceFactory.getService(checkUnitResult.getClass());
        Result result = resultRepo.findById(jobId).orElseGet(Result::new);
        result.setArrangement(arrangement);
        resultsKafkaService.fillResult(result, jobId, checkUnitResult, service);
        DetailResult detailResult = service.getOrCreate(result, checkUnitResult);
        result.setDetailResult(detailResult);

        if(checkUnitResult instanceof AnalysisResult) {
            Optional<Screenshots> screenshotsOpt = resultsKafkaService.getScreenshot(arrangement.getId(), jobId);
            screenshotsOpt.ifPresent(screenshots -> {
                if ((screenshots.getScreenshot() != null && screenshots.getScreenshot().length > 0) ||
                    (screenshots.getEtalonScreenshot() != null && screenshots.getEtalonScreenshot().length > 0)) {
                    ResultScreenShot resultScreenShot = resultScreenShotRepo.findById(jobId).orElseGet(ResultScreenShot::new);
                    resultScreenShot.setResult(result);
                    //Устанавливаем штамп на скриншот
                    resultScreenShot.setScreenshot(imprintScreenshot(accessToolInfo, checkUnitResult, screenshots.getScreenshot(), result.getCheckType()));
                    resultScreenShot.setEtalonScreenshot(screenshots.getEtalonScreenshot());
                    result.setResultScreenShot(resultScreenShot);
                }
            });
        }
        resultRepo.save(result);
    }

    private byte[] imprintScreenshot(Map<String, String> accessToolInfo, CheckUnitResult checkUnitResult, byte[] screenShot, CheckType checkType){
        headerObject.setLabel1(dispatcherProperties.getImprint().getHeader());
        headerObject.setLabel2("Дата: " + new SimpleDateFormat("yyyy-mm-dd").format(checkUnitResult.getEndTime()) +
            " Время: " + new SimpleDateFormat("hh:mm:ss").format(checkUnitResult.getEndTime()));
        if(checkType.equals(CheckType.PS)) {
            headerObject.setLabel3(dispatcherProperties.getImprint().getPs());
        } else if (checkType.equals(CheckType.PASD)){
            headerObject.setLabel3(dispatcherProperties.getImprint().getPasd());
        } else {
            //Неподходящий тип, оставляем пробел
            headerObject.setLabel3(" ");
        }
        //Нужен пробел - требование программы
        String accessToolName = " ";
        String accessToolUrl = " ";
        if(accessToolInfo.entrySet().size()==1){
            accessToolName = accessToolInfo.keySet().toArray()[0].toString();
            accessToolUrl = accessToolInfo.get(accessToolName);
        }
        headerObject.setLabel4(accessToolName);
        headerObject.setLabel5(accessToolUrl);
        headerObject.setLabel6(dispatcherProperties.getImprint().getIrtz());
        headerObject.setLabel7(checkUnitResult.getCheckUnit().getValue());

        try {
            return imageProcessor.processImage(screenShot, headerObject);
        } catch (Exception ex) {
            log.error("Ошибка установки штампа на скриншот: " + checkUnitResult.getCheckUnit().getValue() , ex);
            return screenShot;
        }
    }

    private boolean isArrangementFinished(Arrangement arrangement) {
        long count = resultsKafkaService.getResultsCount(arrangement.getId());
        return count != 0 && arrangement.getCheckUnitsCount() <= count;
    }

    private Map<String, String> getAccessToolInfo(Long arrangementId) {
        String accessTool = pptClient.getAccessTool(arrangementId);
        Map<String, String> accessToolInfo = new HashMap<>();
        if(Strings.isNotEmpty(accessTool)) {
            accessToolInfo = configClient.getRobotInfo(accessTool);
        }
        return accessToolInfo;
    }
}
