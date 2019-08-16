package events.handlers;

import arrangement.ArrangementStatusNotification;
import events.ArrangementChannels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;
import services.arrangement.ArrangementNotificationService;

/**
 * Обработчик сообщений об изменении состояний мероприятий
 * Creation date: 14.08.2019
 * Author: asavin
 */
@Service
@EnableBinding(ArrangementChannels.class)
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ArrangementNotificationsHandler {

    private final ArrangementNotificationService arrangementNotificationService;

    @StreamListener(ArrangementChannels.INPUT_ARRANGEMENT_NOTIFICATIONS)
    public void consumeArrangementNotification(ArrangementStatusNotification arrangementStatusNotification){
        log.info("Received arrangement notification message : " + arrangementStatusNotification.toString());
        try {
            arrangementNotificationService.processNotification(arrangementStatusNotification);
        } catch (Exception ex) {
            log.error("Error processing arrangement notification message: " + arrangementStatusNotification.toString(), ex);
        }
    }

}
