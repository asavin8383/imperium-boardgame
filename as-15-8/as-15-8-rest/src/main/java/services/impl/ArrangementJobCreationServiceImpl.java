package services.impl;

import jobs.ArrangementJob;
import jobs.ERDIJob;
import model.task.Arrangement;
import model.task.ArrangementItem;
import org.springframework.stereotype.Service;
import repositories.ArrangementItemRepository;
import services.ArrangementJobCreationService;

import java.util.ArrayList;
import java.util.List;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 * Сервис создания сообщений на проверку ЕРДИ по мероприятию
 */

@Service
public class ArrangementJobCreationServiceImpl implements ArrangementJobCreationService {

    private ArrangementItemRepository arrangementItemRepo;

    public ArrangementJobCreationServiceImpl(ArrangementItemRepository arrangementItemRepo) {
        this.arrangementItemRepo = arrangementItemRepo;
    }

    @Override
    public List<ArrangementJob> createArrangementJobs(Arrangement arrangement) {
        List<ArrangementJob> jobList = new ArrayList<>();
        for(ArrangementItem arrangementItem : arrangementItemRepo.findAllByArrangementId(arrangement.getId())){
            ArrangementJob arrangementJob = new ArrangementJob();
            arrangementJob.setId(arrangement.getId());
            arrangementJob.setAccessToolUnit(arrangement.getAccessTool().getName());
            arrangementJob.getErdiJobList().add(new ERDIJob(arrangementItem.getErdi().getId()));
            jobList.add(arrangementJob);
        }
        return jobList;
    }
}
