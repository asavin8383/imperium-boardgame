package services.arrangement.impl;

import jobs.ArrangementJob;
import events.ArrangementChannels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.task.Arrangement;
import model.task.FormalTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import repositories.ArrangementRepo;
import services.arrangement.ArrangementJobCreationService;

/**
 * Creation date: 05.08.2019
 * Сервис обработки данных мероприятий
 * Author: asavin
 */

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_={@Autowired})
@EnableBinding(ArrangementChannels.class)
public class ArrangementService {

    private final ArrangementRepo arrangementRepo;
    private final ArrangementJobCreationService arrangementJobCreationService;
    private final ArrangementChannels source;

    public Arrangement saveArrangement(Arrangement arrangement, FormalTask formalTask){
        arrangement.setFormalTask(formalTask);
        return arrangementRepo.save(arrangement);
    }

    /**
     * Отправка мероприятия диспетчеру для детального заполнения
     * @param arrangement мероприятие
     */
    public void fillArrangement(Arrangement arrangement){
        ArrangementJob arrangementJob = arrangementJobCreationService.createArrangementJob(arrangement);
        source
            .outputArrangementJobs()
            .send(
                MessageBuilder
                    .withPayload(arrangementJob)
                    .build()
                    );
        log.info("Мероприятие {} отправлено диспетчеру для заполнения", arrangement.getId());
    }

}
