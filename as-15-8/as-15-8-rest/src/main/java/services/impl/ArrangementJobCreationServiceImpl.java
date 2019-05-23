package services.impl;

import jobs.ArrangementJob;
import model.task.Arrangement;
import model.task.ArrangementItem;
import org.springframework.stereotype.Service;
import repositories.ArrangementItemRepository;
import services.ArrangementJobCreationService;

import java.util.List;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 */

@Service
public class ArrangementJobCreationServiceImpl implements ArrangementJobCreationService {

    private ArrangementItemRepository arrangementItemRepo;

    public ArrangementJobCreationServiceImpl(ArrangementItemRepository arrangementItemRepo) {
        this.arrangementItemRepo = arrangementItemRepo;
    }

    @Override
    public List<ArrangementJob> createArrangementJobs(Arrangement arrangement) {
        for(ArrangementItem arrangementItem : arrangementItemRepo.findAllByArrangementId(arrangement.getId())){

        }
        return null;
    }
}
