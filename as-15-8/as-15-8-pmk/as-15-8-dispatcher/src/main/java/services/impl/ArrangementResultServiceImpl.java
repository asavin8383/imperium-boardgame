package services.impl;

import analysis.AnalysisResult;
import enums.CheckUnitJobResult;
import enums.ExecutionStatus;
import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Result;
import model.DetailResult;
import model.ResultScreenShot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.ResultRepo;
import repositories.DetailResultRepo;
import repositories.ResultScreenShotRepo;
import services.AnalysisResultService;
import services.AnalysisResultServiceFactory;
import services.ArrangementResultService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
@Slf4j
public class ArrangementResultServiceImpl implements ArrangementResultService {

    private final ResultRepo resultRepo;
    private final ResultScreenShotRepo resultScreenShotRepo;
    private final DetailResultRepo detailResultRepo;


    @Override
    @Transactional
    public Result processJobResult(AnalysisResult analysisResult) {
        Result result = findJobByID(analysisResult.getJobID());
        AnalysisResultService<? super AnalysisResult> service = AnalysisResultServiceFactory.getService(analysisResult.getClass());
        result.setEndDate(LocalDateTime.now());
        result.setResult(analysisResult.getCheckResult());
        resultRepo.save(result);
        if((analysisResult.getScreenshot() != null && analysisResult.getScreenshot().length > 0) ||
                (analysisResult.getEtalonScreenshot() != null && analysisResult.getEtalonScreenshot().length > 0)){
            ResultScreenShot resultScreenShot = new ResultScreenShot();
            resultScreenShot.setResult(result);
            resultScreenShot.setScreenshot(analysisResult.getScreenshot());
            resultScreenShot.setEtalonScreenshot(analysisResult.getEtalonScreenshot());
            resultScreenShotRepo.save(resultScreenShot);
        }
        service.processResult(result, analysisResult);
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
        resultRepo.save(result);
        if(status == CheckUnitJobResult.INTERNAL_ERROR)
            saveErrorToDetailResults(result, description);
        return result;
    }

    private Result findJobByID(Long jobID) {
        return resultRepo.findById(jobID)
                .orElseThrow(() ->
                        new AS_15_8_DispatcherException("Ошибка! Задание не найдено! ID: " + jobID)
                );
    }

    private void saveErrorToDetailResults(Result result, String exText) {
        DetailResult detailResult = new DetailResult();
        detailResult.setResult(result);
        detailResult.setResponseError(false);
        detailResult.setStubScoreInfo(exText);
        detailResultRepo.save(detailResult);
    }
}
