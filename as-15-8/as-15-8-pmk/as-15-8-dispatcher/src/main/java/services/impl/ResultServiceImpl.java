package services.impl;

import analysis.AnalysisResult;
import arrangement.ArrangementStatusNotification;
import enums.ArrangementEvents;
import enums.CheckUnitJobResult;
import enums.ErdiStatus;
import enums.ExecutionStatus;
import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.ErrorDetailResult;
import model.Result;
import model.ResultScreenShot;
import model.enums.CheckType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.ErrorDetailResultRepo;
import repositories.ResultRepo;
import repositories.ResultScreenShotRepo;
import restapi.ArrangementStatusProducer;
import restapi.ErdiChecker;
import services.AnalysisResultService;
import services.AnalysisResultServiceFactory;
import services.ResultService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
@Slf4j
public class ResultServiceImpl implements ResultService {

    private final ResultRepo resultRepo;
    private final ResultScreenShotRepo resultScreenShotRepo;
    private final ErrorDetailResultRepo errorDetailResultRepo;
    private final ErdiChecker erdiChecker;
    private final ArrangementStatusProducer arrangementStatusProducer;
    @Override
    @Transactional
    public Result saveJobResult(AnalysisResult analysisResult) {
        Result result = findJobByID(analysisResult.getJobID());
        AnalysisResultService<? super AnalysisResult> service = AnalysisResultServiceFactory.getService(analysisResult.getClass());
        result.setEndDate(LocalDateTime.now());
        result.setResult(checkStatus(result.getErdiId(), analysisResult.getCheckResult()));

        if(analysisResult.getCheckResult().equals(CheckUnitJobResult.INTERNAL_ERROR)) {
            result.setCheckType(CheckType.ERROR);
            saveResultAsError(result, service.getErrorText(analysisResult));
        } else {
            result.setCheckType(service.getCheckType());
            if(analysisResult.getCheckResult().equals(CheckUnitJobResult.FORBIDDEN_CONTENT_DETECTED))
                result.setCheckForAct(true);
            service.saveResult(result, analysisResult);
        }
        if((analysisResult.getScreenshot() != null && analysisResult.getScreenshot().length > 0) ||
                (analysisResult.getEtalonScreenshot() != null && analysisResult.getEtalonScreenshot().length > 0)){
            ResultScreenShot resultScreenShot = resultScreenShotRepo.findById(result.getId()).orElseGet(ResultScreenShot::new);
            resultScreenShot.setResult(result);
            resultScreenShot.setScreenshot(analysisResult.getScreenshot());
            resultScreenShot.setEtalonScreenshot(analysisResult.getEtalonScreenshot());
            resultScreenShotRepo.save(resultScreenShot);
        }
        return result;
    }

    @Override
    public ExecutionStatus checkArrangementStatus(Long arrangementID) {
        Long notFinishedJobsCount = resultRepo.countByResultNullOrResultIn(arrangementID,
                Arrays.asList(
                        CheckUnitJobResult.PLANNED,
                        CheckUnitJobResult.RUNNING)
        );
        return notFinishedJobsCount > 0 ? ExecutionStatus.RUNNING : ExecutionStatus.FINISHED;
    }

    @Override
    @Transactional
    public Result updateJobStatus(Long jobID, Long erdiID, CheckUnitJobResult status, String description) {
        Result result = findJobByID(jobID);
        result.setResult(checkStatus(erdiID, status));
        result.setEndDate(LocalDateTime.now());
        if(status == CheckUnitJobResult.INTERNAL_ERROR || status == CheckUnitJobResult.TIMEOUT_ERROR) {
            result.setCheckType(CheckType.ERROR);
            saveResultAsError(result, description);
        } else {
            resultRepo.save(result);
        }
        return result;
    }

    private Result findJobByID(Long jobID) {
        return resultRepo.findById(jobID)
                .orElseThrow(() ->
                        new AS_15_8_DispatcherException("Ошибка! Задание не найдено! ID: " + jobID)
                );
    }

    private void saveResultAsError(Result result, String exText) {
        ErrorDetailResult errorDetailResult = errorDetailResultRepo.findById(result.getId())
                .orElseGet(ErrorDetailResult::new);

        errorDetailResult.setResult(result);

        errorDetailResult.setError(exText);
        errorDetailResultRepo.save(errorDetailResult);
    }

    private CheckUnitJobResult checkStatus(Long erdiId, CheckUnitJobResult status){
        if(erdiChecker.checkErdiStatus(erdiId).equals(ErdiStatus.EXCLUDED))
            return CheckUnitJobResult.EXCLUDED;
        else
            return status;
    }

    public void sendNotificationsIfFinished(Long arrangementId) {
        ExecutionStatus status = checkArrangementStatus(arrangementId);
        if(status == ExecutionStatus.FINISHED) {
            log.info("Мероприятие успешно завешено: " + arrangementId);
            arrangementStatusProducer.sendArrangementStatusMessage(new ArrangementStatusNotification(arrangementId, ArrangementEvents.FINISH));
        }
    }
}
