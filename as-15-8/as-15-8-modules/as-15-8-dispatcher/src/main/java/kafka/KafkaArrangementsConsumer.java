package kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import checkUnits.CheckUnitJob;
import jobs.ArrangementJob;
import lombok.extern.slf4j.Slf4j;
import services.checkUnitJob.CheckUnitJobService;

@Service
@Slf4j
public class KafkaArrangementsConsumer {

	private CheckUnitJobService checkUnitJobService;
	
	private KafkaTemplate<String, CheckUnitJob> checkUnitJobKafkaTemplate;

	@Value("${spring.kafka.jobs-topic}")
	private String checkUnitJobTopicName;

	@Autowired
	public KafkaArrangementsConsumer(CheckUnitJobService checkUnitJobService,
									 KafkaTemplate<String, CheckUnitJob> checkUnitJobKafkaTemplate) {
		this.checkUnitJobService = checkUnitJobService;
		this.checkUnitJobKafkaTemplate = checkUnitJobKafkaTemplate;
	}

	@KafkaListener(topics = "${spring.kafka.arrangements-topic}")
    public void consumeArrangementJob(ArrangementJob arrangementJob, Acknowledgment ack) {
		log.info("Принято задание на проведение мероприятия: " + arrangementJob.toString());
        CompletableFuture.runAsync(() -> {
        	try {
				checkUnitJobService.prepareJobs(arrangementJob)
					.forEach(checkUnitJob -> {
						//TODO обеспечить гарантию сохранения результата и отправки сообщения
						checkUnitJobService.saveCheckUnitJobAsResult(checkUnitJob);
						send(checkUnitJob);
					});
        	} catch (Exception ex) {
        		log.error("Ошибка при обработке задания на проведение мероприятия: " + arrangementJob.toString(), ex);
        	}
        	ack.acknowledge();
        });
    }

	private void send(CheckUnitJob checkUnitJob) {
		try {
			Message<CheckUnitJob> message = MessageBuilder
					.withPayload(checkUnitJob)
					.setHeader(KafkaHeaders.TOPIC, checkUnitJobTopicName)
					.build();
			ListenableFuture<SendResult<String, CheckUnitJob>> future = checkUnitJobKafkaTemplate.send(message);

			future.addCallback(new ListenableFutureCallback<SendResult<String, CheckUnitJob>>() {

				@Override
				public void onSuccess(SendResult<String, CheckUnitJob> result) {
					log.info("Check unit job message was sent: " +
							"arrangement ID: " + result.getProducerRecord().value().getArrangementID() + ", " +
							"access tool: " + result.getProducerRecord().value().getAccessToolUnit() + ", " +
							"check unit value: " + result.getProducerRecord().value().getCheckUnit().getValue()
							);
				}

				@Override
				public void onFailure(Throwable ex) {
					throw new RuntimeException(ex);
				}
			});
		} catch (Exception ex) {
			throw new RuntimeException("Error sending arrangement job message: ", ex);
		}
	}
	
}
