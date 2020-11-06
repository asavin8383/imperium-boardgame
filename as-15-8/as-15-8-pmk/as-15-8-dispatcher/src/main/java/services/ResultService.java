package services;

import accessTools.AccessToolDTO;
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
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.LocalDateTimeType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.ArrangementRepo;
import restapi.ArrangementRestApi;
import restapi.ConfigClient;
import restapi.PptClient;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Objects;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ResultService {

    private final ResultsKafkaService resultsKafkaService;

    private final ArrangementRepo arrangementRepo;
    private final ArrangementRestApi arrangementRestApi;
    private final ArrangementService arrangementService;
    //Объекты для создания штампа на скриншоте
    private final ImageProcessor imageProcessor = new ImageProcessor();
    private final HeaderObject headerObject = new HeaderObject();
    private final DispatcherProperties dispatcherProperties;
    private final ConfigClient configClient;
    private final PptClient pptClient;

    private final EntityManagerFactory entityManagerFactory;

    @Value("${results.transaction.batch.size}")
    private int transactionBatchSize;

    @Value("${results.log.every}")
    private int logEvery;

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
                    .forEach(this::saveArrangement);
        } catch (Exception ex){
            log.error("Ошибка при сохранении результатов мероприятий", ex);
        }
    }

    public void saveArrangement(Long arrangementId){
        Arrangement arrangement = arrangementRepo
                .findById(arrangementId)
                .orElseThrow((() -> new AS_15_8_DispatcherException("Ошибка! Мероприятие для выгрузки не найдено в БД по id: " + arrangementId)));
        arrangement.setStatus(ArrangementStatus.UPLOADING);
        arrangementRepo.save(arrangement);
        saveArrangement(arrangement);
    }

    private void logEvery_N_Result(int transactionCount, String objectName) {
        if (transactionCount % logEvery == 0)
            log.info("Записано {} {}", transactionCount, objectName);
    }

    @Transactional
    void saveArrangement(Arrangement arrangement) {
        log.info("Начато сохранение мероприятия: " + arrangement.getId());
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        boolean isStopped = !arrangementService.isArrangementRunning(arrangement.getId(), arrangement.getVersion());
        try {
            KeyValueIterator<Windowed<CheckUnitKey>, CheckUnitResult> resultsIterator =
                    resultsKafkaService.getArrangementResultsIterator(arrangement.getId())
                    .orElseThrow(() -> new Exception("Не удалось получить результаты мероприятия из временного хранилища"));
            KeyValueIterator<Windowed<CheckUnitKey>, Screenshots> screenshotsIterator =
                    resultsKafkaService.getArrangementResultScreenshotsIterator(arrangement.getId())
                    .orElseThrow(() -> new Exception("Не удалось получить скриншоты результатов мероприятия из временного хранилища"));

            boolean isSaved = saveArrangementResults(arrangement, resultsIterator, entityManager);
            saveArrangementScreenshots(arrangement, screenshotsIterator, entityManager);

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
        } catch (Exception ex){
            log.error("Ошибка при сохранении результатов проверок мероприятия " + arrangement.getId(), ex);
            arrangement.setStatus(isStopped ? ArrangementStatus.STOPPING : ArrangementStatus.RUNNING);
            arrangementRepo.save(arrangement);
        } finally {
            entityManager.close();
        }
    }

    @Async
    boolean saveArrangementResults(
            Arrangement arrangement,
            KeyValueIterator<Windowed<CheckUnitKey>, CheckUnitResult> resultsIterator,
            EntityManager entityManager){
        boolean isSaved = true;
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            int transactionCount = 0;
            while (resultsIterator.hasNext()) {

                if (transactionCount % transactionBatchSize == 0)
                    transaction.begin();

                KeyValue<Windowed<CheckUnitKey>, CheckUnitResult> windowedResult = resultsIterator.next();
                KeyValue<CheckUnitKey, CheckUnitResult> result = KeyValue.pair(windowedResult.key.key(), windowedResult.value);

                if (!saveArrangementResult(entityManager, arrangement, result.key, result.value))
                    isSaved = false;

                transactionCount++;

                if (transactionCount % transactionBatchSize == 0)
                    transaction.commit();

                logEvery_N_Result(transactionCount, "результатов");
            }

            //TODO убрать!
            try {
                Thread.sleep(300000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (transaction.isActive()) {
                transaction.commit();
                log.info("Записано {} результатов", transactionCount);
            }
        } catch (Exception ex) {
            if(transaction.isActive())
                transaction.rollback();
        }
        return isSaved;
    }

    private void saveArrangementScreenshots(
            Arrangement arrangement,
            KeyValueIterator<Windowed<CheckUnitKey>, Screenshots> screenshotsIterator,
            EntityManager entityManager){
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            int transactionCount = 0;
            while (screenshotsIterator.hasNext()) {

                if (transactionCount % transactionBatchSize == 0)
                    transaction.begin();

                KeyValue<Windowed<CheckUnitKey>, Screenshots> windowedResult = screenshotsIterator.next();
                KeyValue<CheckUnitKey, Screenshots> screenshot = KeyValue.pair(windowedResult.key.key(), windowedResult.value);

                //Если штамп ставим, нужно попросить инфо об AccessTool
                AccessToolDTO accessToolDTO = dispatcherProperties.getImprint().isUseImprint() ? getAccessToolInfo(arrangement.getId()) : null;

                saveScreenshot(screenshot.value, entityManager, screenshot.key.getJobId(), accessToolDTO);

                transactionCount++;
                if (transactionCount % transactionBatchSize == 0)
                    transaction.commit();
                logEvery_N_Result(transactionCount, "скриншотов");
            }

            if (transaction.isActive()) {
                transaction.commit();
                log.info("Записано {} скриншотов", transactionCount);
            }
        } catch (Exception ex) {
            if(transaction.isActive())
                transaction.rollback();
        }
    }

    private boolean saveArrangementResult(EntityManager entityManager, Arrangement arrangement, CheckUnitKey checkUnitKey, CheckUnitResult checkUnitResult) {
        try {
            DetailResultService<? super CheckUnitResult, ? extends DetailResult> service = AnalysisResultServiceFactory.getService(checkUnitResult.getClass());
            //       Result result = resultRepo.findById(jobId).orElseGet(Result::new);
            Result result = new Result();
            result.setArrangement(arrangement);
            resultsKafkaService.fillResult(result, checkUnitKey.getJobId(), checkUnitResult, service);
            //DetailResult detailResult = service.getOrCreate(result, checkUnitResult);
            DetailResult detailResult = service.create(checkUnitResult);
            detailResult.setId(checkUnitKey.getJobId());

            upsertResult(entityManager, result);
            service.save(entityManager, detailResult);
        } catch (Exception ex) {
            saveErrorResult(ex, checkUnitKey, checkUnitResult, arrangement, entityManager);
            return false;
        }
        return true;
    }

    private void saveScreenshot(Screenshots screenshots, EntityManager entityManager, Long jobId, AccessToolDTO accessToolDTO){
        if ((screenshots.getScreenshot() != null && screenshots.getScreenshot().length > 0) ||
                (screenshots.getEtalonScreenshot() != null && screenshots.getEtalonScreenshot().length > 0)) {
            //ResultScreenShot resultScreenShot = resultScreenShotRepo.findById(jobId).orElseGet(ResultScreenShot::new);

            ResultScreenShot resultScreenShot = new ResultScreenShot();
            resultScreenShot.setId(jobId);

//                if(dispatcherProperties.getImprint().isUseImprint()){
//                    //Устанавливаем штамп на скриншот
//                    resultScreenShot.setScreenshot(imprintScreenshot(accessToolDTO, checkUnitResult, screenshots.getScreenshot(), checkType));
//                } else {
//                    //Без штампа
//                }
            resultScreenShot.setScreenshot(screenshots.getScreenshot());
            resultScreenShot.setEtalonScreenshot(screenshots.getEtalonScreenshot());
            upsertResultScreenShot(entityManager, resultScreenShot);
        }
    }

    private void saveErrorResult(Exception ex, CheckUnitKey checkUnitKey, CheckUnitResult checkUnitResult, Arrangement arrangement, EntityManager entityManager){
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

            saveArrangementResult(entityManager, arrangement, checkUnitKey, notification);
            log.info("Ошибка сохранена: " + checkUnitKey.getJobId());
        } catch (Exception newEx) {
            log.error("Ошибка при сохранении ошибочной обработки сообщения с анализом результатов проверки: " + checkUnitKey.getJobId() + ", " + checkUnitResult.getCheckUnit().getValue(), newEx);
        }
    }

    private void upsertResult(EntityManager entityManager, Result result){
        String sql = "insert into results.results " +
            "(id, arrangement_id, content_id, result, start_date, end_date, check_type, check_unit_type, check_unit_value) " +
            "values " +
            "(:id, :arrangementId, :contentId, :result, :startDate, :endDate, :checkType, :checkUnitType, :checkUnitValue) " +
            "on conflict(id) do update " +
            "set " +
            "id = :id, " +
            "arrangement_id = :arrangementId, " +
            "content_id = :contentId, " +
            "result = :result, " +
            "start_date = :startDate, " +
            "end_date = :endDate, " +
            "check_type = :checkType, " +
            "check_unit_type = :checkUnitType, " +
            "check_unit_value = :checkUnitValue";
        NativeQuery nativeQuery = entityManager.createNativeQuery(sql).unwrap(NativeQuery.class);
        nativeQuery.setParameter("id", result.getId());
        nativeQuery.setParameter("arrangementId", result.getArrangement().getId(), LongType.INSTANCE);
        nativeQuery.setParameter("contentId", result.getErdiId(), LongType.INSTANCE);
        nativeQuery.setParameter("result", result.getResult().name(), StringType.INSTANCE);
        nativeQuery.setParameter("startDate", result.getStartDate(), LocalDateTimeType.INSTANCE);
        nativeQuery.setParameter("endDate", result.getEndDate(), LocalDateTimeType.INSTANCE);
        nativeQuery.setParameter("checkType", result.getCheckType().name(), StringType.INSTANCE);
        nativeQuery.setParameter("checkUnitType", result.getCheckUnitType().name(), StringType.INSTANCE);
        nativeQuery.setParameter("checkUnitValue", result.getCheckUnitValue(), StringType.INSTANCE);

        log.info("Результат с id: {} загружен", result.getId());

        nativeQuery.executeUpdate();
    }

    private void upsertResultScreenShot(EntityManager entityManager, ResultScreenShot resultScreenShot){
        String sql = "insert into results.result_screenshots " +
            "(result_id, screenshot, etalon_screenshot) " +
            "values " +
            "(:id, :screenshot, :etalonScreenshot) " +
            "on conflict(result_id) do update " +
            "set " +
            "result_id = :id, " +
            "screenshot = :screenshot, " +
            "etalon_screenshot = :etalonScreenshot";
        NativeQuery nativeQuery = entityManager.createNativeQuery(sql).unwrap(NativeQuery.class);
        nativeQuery.setParameter("id", resultScreenShot.getId());
        nativeQuery.setParameter("screenshot", resultScreenShot.getScreenshot());
        nativeQuery.setParameter("etalonScreenshot", resultScreenShot.getEtalonScreenshot());

        log.info("Скриншот с id: {} загружен", resultScreenShot.getId());

        nativeQuery.executeUpdate();
    }

    private byte[] imprintScreenshot(AccessToolDTO accessToolDTO, CheckUnitResult checkUnitResult, byte[] screenShot, CheckType checkType){
        headerObject.setLabel1(dispatcherProperties.getImprint().getHeader());
        headerObject.setLabel2("Дата: " + new SimpleDateFormat("dd.MM.yyyy").format(checkUnitResult.getEndTime()));
        if(checkType.equals(CheckType.PS)) {
            headerObject.setLabel3(dispatcherProperties.getImprint().getPs());
        } else if (checkType.equals(CheckType.PASD)){
            headerObject.setLabel3(dispatcherProperties.getImprint().getPasd());
        } else {
            //Неподходящий тип, оставляем пробел - требование программы
            headerObject.setLabel3(" ");
        }
        headerObject.setLabel4(accessToolDTO.getOriginalName());
        headerObject.setLabel5(accessToolDTO.getUrl());
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

    private AccessToolDTO getAccessToolInfo(Long arrangementId) {
        String accessTool = pptClient.getAccessTool(arrangementId);
        if(Strings.isNotEmpty(accessTool)) {
            return configClient.getRobotInfo(accessTool);
        }
        //Возвращаем строки с пробелом, чтобы не сломать скриншот
        return new AccessToolDTO(accessTool, " ", " ");
    }
}
