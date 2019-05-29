package kafka;

import arrangement.ArrangementStatusNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import services.arrangement.ArrangementNotificationService;

import java.util.concurrent.CompletableFuture;

/**
 * Creation date: 28.05.2019
 * Author: asavin
 * Сервис обработки сообщений об изменении статуса мероприятия
 */
@Service
@Slf4j
public class ArrangementNotificationConsumer {

    private ArrangementNotificationService arrangementNotificationService;

    @Autowired
    public ArrangementNotificationConsumer(ArrangementNotificationService arrangementNotificationService) {
        this.arrangementNotificationService = arrangementNotificationService;
    }

    @KafkaListener(topics = "${spring.kafka.arrangement-notification-topic}"
            , containerFactory = "kafkaListenerContainerFactory")
    public void consumeArrangementNotification(ArrangementStatusNotification arrangementStatusNotification, Acknowledgment ack){
        log.info("Received arrangement notification message : " + arrangementStatusNotification.toString());
        CompletableFuture.runAsync(() -> {
            try {
                arrangementNotificationService.processNotification(arrangementStatusNotification);
            } catch (Exception ex) {
                log.error("Error processing arrangement notification message: " + arrangementStatusNotification.toString(), ex);
            }
            ack.acknowledge();
        });
    }
}
