package services.arrangement.impl;

import jobs.ArrangementJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import services.arrangement.ArrangementJobExecutionService;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 */
@Slf4j
@Service
public class ArrangementJobExecutionServiceImpl implements ArrangementJobExecutionService {

    

    @Override
    public void run(ArrangementJob arrangementJob) {
        /*try {
            Message<ArrangementJob> message = MessageBuilder
                .withPayload(arrangementJob)
                .setHeader(KafkaHeaders.TOPIC, arrangementJobTopicName)
                .build();
            ListenableFuture<SendResult<String, ArrangementJob>> future = kafkaTemplate.send(message);
            log.info("Arrangement job message was sent to dispatcher: " + message);

            future.addCallback(new ListenableFutureCallback<SendResult<String, ArrangementJob>>() {

                @Override
                public void onSuccess(SendResult<String, ArrangementJob> result) {
                    log.info("Arrangement job message was sent: " +
                            "arrangenmentID: " + result.getProducerRecord().value().getId() + ", " +
                            "access tool: " + result.getProducerRecord().value().getAccessToolUnit());
                }

                @Override
                public void onFailure(Throwable ex) {
                    throw new RuntimeException(ex);
                }
            });
        } catch (Exception ex) {
            throw new AS_15_8_Exception("Error sending arrangement job message: ", ex);
        }*/
    }
}
