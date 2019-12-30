package events.producers;

import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitKey;
import events.PPM_Channels;
import exceptions.AS_15_8_PPM_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * Created by san
 * Date: 05.11.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@EnableBinding(PPM_Channels.class)
public class CheckUnitJobProducer {

    private final PPM_Channels ppm_channels;

    /**
     * Метод отправки задания диспетчеру в тему Kafka
     * @param checkUnitJob Задание на проверку чек-юнита
     */
    public void sendJobToDispatcher(CheckUnitJob checkUnitJob, CheckUnitKey key, int partitionId) {
        try {
            log.info("Отправка сообщения с заданием на проверку диспетчеру. Раздел: {}, тело: {}", partitionId, checkUnitJob);
            Message<CheckUnitJob> message = MessageBuilder
                    .withPayload(checkUnitJob)
                    .setHeader(KafkaHeaders.PARTITION_ID, partitionId)
                    .setHeader(KafkaHeaders.MESSAGE_KEY, key)
                    .build();

            boolean send = ppm_channels.outputJobs().send(message);
            if(send)
                log.info("Сообщение успешно отправлено: " + checkUnitJob.getArrangementId() + ", " + checkUnitJob.getCheckUnit().getValue());
        } catch (Exception ex) {
            throw new AS_15_8_PPM_Exception("Ошибка при отправке задания диспетчеру", ex);
        }
    }
}
