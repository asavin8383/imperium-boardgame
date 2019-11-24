package services.impl;

import analysis.AnalysisResult;
import enums.CheckUnitJobResult;
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


    @Override
    @Transactional
    public Result saveJobResult(AnalysisResult analysisResult) {
        Result result = findJobByID(analysisResult.getJobID());
        AnalysisResultService<? super AnalysisResult> service = AnalysisResultServiceFactory.getService(analysisResult.getClass());
        result.setEndDate(LocalDateTime.now());
        result.setCheckType(service.getCheckType());
        result.setResult(analysisResult.getCheckResult());
        if((analysisResult.getScreenshot() != null && analysisResult.getScreenshot().length > 0) ||
                (analysisResult.getEtalonScreenshot() != null && analysisResult.getEtalonScreenshot().length > 0)){
            ResultScreenShot resultScreenShot = new ResultScreenShot();
            resultScreenShot.setResult(result);
            resultScreenShot.setScreenshot(analysisResult.getScreenshot());
            resultScreenShot.setEtalonScreenshot(analysisResult.getEtalonScreenshot());
            resultScreenShotRepo.save(resultScreenShot);
        }
        service.saveResult(result, analysisResult);
        return result;
    }

    @Override
    public ExecutionStatus checkArrangementStatus(Long arrangementID) {
        Long notFinishedJobsCount = resultRepo.countByResultNullOrResultIn(arrangementID,
                Arrays.asList(
                        CheckUnitJobResult.PLANNED,
                        CheckUnitJobResult.RUNNING,
                        CheckUnitJobResult.CAPTCHA_DETECTED)
        );
        return notFinishedJobsCount > 0 ? ExecutionStatus.RUNNING : ExecutionStatus.FINISHED;
    }

    @Override
    @Transactional
    public Result updateJobStatus(Long jobID, CheckUnitJobResult status, String description) {
        Result result = findJobByID(jobID);
        result.setResult(status);
        result.setEndDate(LocalDateTime.now());
        if(status == CheckUnitJobResult.INTERNAL_ERROR) {
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
        ErrorDetailResult errorDetailResult = new ErrorDetailResult();
        errorDetailResult.setResult(result);
        errorDetailResult.setError(exText);
        errorDetailResultRepo.save(errorDetailResult);
    }
}
