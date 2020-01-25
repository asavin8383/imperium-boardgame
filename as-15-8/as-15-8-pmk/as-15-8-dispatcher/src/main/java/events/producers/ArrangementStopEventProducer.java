package events.producers;

import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.bus.SpringCloudBusClient;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import remoteEvents.ArrangementStopEvent;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@EnableBinding({SpringCloudBusClient.class})
public class ArrangementStopEventProducer {

    private final SpringCloudBusClient springCloudBusClient;

    public void send(ArrangementStopEvent event) {
        try {
            log.info("Отправка сообщения с заданием на остановку мероприятия. id: " + event.getArrangementId() + ", version: " + event.getVersion());
            Message<ArrangementStopEvent> message = MessageBuilder
                    .withPayload(event)
                    .build();

            boolean send = springCloudBusClient.springCloudBusInput().send(message);
            if(send)
                log.info("Сообщение успешно отправлено: " + event.toString());
        } catch (Exception ex) {
            throw new AS_15_8_DispatcherException("Ошибка при отправке задания на остановку мероприятия. id: " + event.getArrangementId() + ", version: " + event.getVersion(), ex);
        }
    }
}
