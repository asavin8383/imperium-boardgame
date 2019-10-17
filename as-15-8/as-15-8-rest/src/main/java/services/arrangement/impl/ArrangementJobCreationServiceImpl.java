package services.arrangement.impl;

import exceptions.AS_15_8_Exception;
import jobs.ArrangementJob;
import jobs.ERDIJob;
import lombok.RequiredArgsConstructor;
import model.task.Arrangement;
import model.task.ArrangementItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.ArrangementItemRepository;
import services.arrangement.ArrangementJobCreationService;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 * Сервис создания сообщений на проверку ЕРДИ по мероприятию
 */

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ArrangementJobCreationServiceImpl implements ArrangementJobCreationService {

    private final ArrangementItemRepository arrangementItemRepo;

    @Override
    public ArrangementJob createArrangementJob(Arrangement arrangement) {
        ArrangementJob arrangementJob = createBriefArrangementJob(arrangement);
        //Установим тип запуска для диспетчеризации старта/перезапуска
        arrangementJob.setRunType(getRunType(arrangement));
        arrangementJob.setAccessTool(arrangement.getAccessTool());
        for(ArrangementItem arrangementItem : arrangementItemRepo.findAllByArrangementId(arrangement.getId())){
            arrangementJob.getErdiJobList().add(new ERDIJob(arrangementItem.getErdi().getId()));
        }
        return arrangementJob;
    }

    @Override
    public ArrangementJob createBriefArrangementJob(Arrangement arrangement){
        ArrangementJob arrangementJob = new ArrangementJob();
        arrangementJob.setId(arrangement.getId());
        return arrangementJob;
    }

    private ArrangementJob.JobRunType getRunType(Arrangement arrangement){
        switch (arrangement.getStatus()){
            case NEW:
                return ArrangementJob.JobRunType.START;
            case ACTION_REQUIRED:
                return ArrangementJob.JobRunType.RESTART;
            default:
                throw new AS_15_8_Exception("Error creating arrangement job! Status is not supported: " + arrangement.getStatus());
        }
    }
}
