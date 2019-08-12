package events.handlers;

import arrangement.ArrangementStatusNotification;
import enums.ExecutionStatus;
import events.DispatcherChannels;
import exceptions.AS_15_8_DispatcherException;
import jobs.ArrangementJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import services.CheckUnitJobService;

/**
 * Обработчик входящих сообщений от рест-сервиса с мероприятиями для заполнения
 * Creation date: 06.08.2019
 * Author: asavin
 */
@Slf4j
@EnableBinding(DispatcherChannels.class)
@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class ArrangementHandler {

    private final CheckUnitJobService checkUnitJobService;

    /**
     * Заполнение проверяемых ресурсов по ЕРДИ мероприятия
     * @param arrangementJob
     */
    @StreamListener(DispatcherChannels.INPUT)
    @SendTo(DispatcherChannels.INPUT)
    public ArrangementStatusNotification createJobItems(ArrangementJob arrangementJob){
        log.info("Принято задание на проведение мероприятия: " + arrangementJob.toString());
        try {
            if(arrangementJob.getRunType().equals(ArrangementJob.JobRunType.START)) {
                checkUnitJobService.prepareJobs(arrangementJob);
                return new ArrangementStatusNotification(arrangementJob.getId(), ExecutionStatus.FORMED);
            } else {
                log.error("Тип запуска мероприятия не поддерживается: " + arrangementJob.getRunType());
                throw new AS_15_8_DispatcherException("Тип запуска мероприятия не поддерживается: " + arrangementJob.getRunType());
            }
        } catch (Exception ex) {
            log.error("Ошибка при обработке задания на проведение мероприятия: " + arrangementJob.toString(), ex);
            return new ArrangementStatusNotification(arrangementJob.getId(), ExecutionStatus.ERROR);
        }
    }

}
