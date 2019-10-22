package services.impl;

import analysis.AnalysisResult;
import checkUnits.CheckUnitJob;
import enums.CheckUnitJobResult;
import enums.ExecutionStatus;
import exceptions.AS_15_8_DispatcherException;
import jobs.ArrangementJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.ArrangementResult;
import model.DetailResultsVpn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.ArrangementResultRepository;
import repositories.DetailResultsVpnRepository;
import services.AnalysisResultService;
import services.AnalysisResultServiceFactory;
import services.ArrangementResultService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
@Slf4j
public class ArrangementResultServiceImpl implements ArrangementResultService {

    private final ArrangementResultRepository arrangementResultRepo;
    private final DetailResultsVpnRepository detailResultsRepo;

    @Override
    public List<CheckUnitJob> prepareJobs(ArrangementJob arrangementJob) {
        //TODO заглушка, убрать
        return new ArrayList<>();
        /*switch (arrangementJob.getRunType()){
            case START:
                return prepareJobsForStart(arrangementJob);
            default:
                throw new AS_15_8_DispatcherException("Error preparing check unit jobs! Arrangement run type is not supported: " + arrangementJob.getRunType());
        }*/
    }

    @Override
    public ArrangementResult processJobResult(AnalysisResult result) {
        ArrangementResult job = findJobByID(result.getJobID());
        AnalysisResultService<? super AnalysisResult> service = AnalysisResultServiceFactory.getService(result.getClass());
        job.setResult(service.processResult(result));
        job.setScreenshot(result.getScreenshot());
        job.setEtalonScreenshot(result.getEtalonScreenshot());
        job.setEndDate(LocalDateTime.now());
        return arrangementResultRepo.save(job);
    }

    @Override
    public ExecutionStatus checkArrangementStatus(Long arrangementID) {
        Long notFinishedJobsCount = arrangementResultRepo.countByResultNullOrResultIn(arrangementID,
                Arrays.asList(
                        CheckUnitJobResult.PLANNED,
                        CheckUnitJobResult.RUNNING,
                        CheckUnitJobResult.CAPTCHA_DETECTED)
        );
        return notFinishedJobsCount > 0 ? ExecutionStatus.RUNNING : ExecutionStatus.FINISHED;
    }

    @Override
    public ArrangementResult updateJobStatus(Long jobID, CheckUnitJobResult status, String description) {
        ArrangementResult job = findJobByID(jobID);
        job.setResult(status);
        job.setEndDate(LocalDateTime.now());
        if(status == CheckUnitJobResult.INTERNAL_ERROR)
            saveErrorToDetailResults(jobID, description);
        return arrangementResultRepo.save(job);
    }

    private ArrangementResult findJobByID(Long jobID) {
        return arrangementResultRepo.findById(jobID)
                .orElseThrow(() ->
                        new AS_15_8_DispatcherException("Ошибка! Задание не найдено! ID: " + jobID)
                );
    }

    private void saveErrorToDetailResults(Long jobID, String exText) {
        DetailResultsVpn detailResults = new DetailResultsVpn();
        detailResults.setId(jobID);
        detailResults.setResponseError(false);
        detailResults.setStubScoreInfo(exText);
        detailResultsRepo.save(detailResults);
    }
}
