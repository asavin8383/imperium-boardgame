package services.impl;

import analysis.AnalysisResult;
import enums.CheckUnitJobResult;
import enums.ExecutionStatus;
import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Result;
import model.DetailResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.ResultRepo;
import repositories.DetailResultRepo;
import services.AnalysisResultService;
import services.AnalysisResultServiceFactory;
import services.ArrangementResultService;

import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
@Slf4j
public class ArrangementResultServiceImpl implements ArrangementResultService {

    private final ResultRepo resultRepo;
    private final DetailResultRepo detailResultRepo;


    @Override
    public Result processJobResult(AnalysisResult result) {
        Result job = findJobByID(result.getJobID());
        AnalysisResultService<? super AnalysisResult> service = AnalysisResultServiceFactory.getService(result.getClass());
        job.setResult(service.processResult(result));
        job.setScreenshot(result.getScreenshot());
        job.setEtalonScreenshot(result.getEtalonScreenshot());
        job.setEndDate(LocalDateTime.now());
        return resultRepo.save(job);
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
    public Result updateJobStatus(Long jobID, CheckUnitJobResult status, String description) {
        Result job = findJobByID(jobID);
        job.setResult(status);
        job.setEndDate(LocalDateTime.now());
        if(status == CheckUnitJobResult.INTERNAL_ERROR)
            saveErrorToDetailResults(jobID, description);
        return resultRepo.save(job);
    }

    private Result findJobByID(Long jobID) {
        return resultRepo.findById(jobID)
                .orElseThrow(() ->
                        new AS_15_8_DispatcherException("Ошибка! Задание не найдено! ID: " + jobID)
                );
    }

    private void saveErrorToDetailResults(Long jobID, String exText) {
        DetailResult detailResult = new DetailResult();
        Result result = resultRepo.findById(jobID)
                .orElseThrow(() -> AS_15_8_DispatcherException.logAndGet(log, String.format("Результат c ИД %d не найден в БД", jobID)));
        detailResult.setId(jobID);
        detailResult.setResponseError(false);
        detailResult.setStubScoreInfo(exText);
        detailResultRepo.save(detailResult);
    }
}
