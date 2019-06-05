package kafka;

import checkUnits.CheckUnitJob;
import control.ExecutorControlMessage;
import enums.AccessToolUnit;
import exceptions.AS_15_8_DispatcherException;
import jobs.ArrangementJob;
import lombok.extern.slf4j.Slf4j;
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
import services.CheckUnitJobService;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class KafkaArrangementsConsumer {

	private CheckUnitJobService checkUnitJobService;
	
	private KafkaTemplate<String, CheckUnitJob> checkUnitJobKafkaTemplate;
	private KafkaTemplate<String, ExecutorControlMessage> controlMessagesTemplate;


	@Value("${spring.kafka.jobs-topic}")
	private String checkUnitJobTopicName;

	@Value("${spring.kafka.control-topic}")
	private String controlTopicName;

	@Autowired
	public KafkaArrangementsConsumer(CheckUnitJobService checkUnitJobService,
									 KafkaTemplate<String, CheckUnitJob> checkUnitJobKafkaTemplate,
									 KafkaTemplate<String, ExecutorControlMessage> controlMessagesTemplate) {
		this.checkUnitJobService = checkUnitJobService;
		this.checkUnitJobKafkaTemplate = checkUnitJobKafkaTemplate;
		this.controlMessagesTemplate = controlMessagesTemplate;
	}

	@KafkaListener(
		topics = "${spring.kafka.arrangement-tasks-topic}",
		containerFactory = "kafkaListenerContainerFactory"
	)
    public void consumeArrangementJob(ArrangementJob arrangementJob, Acknowledgment ack) {
		log.info("Принято задание на проведение мероприятия: " + arrangementJob.toString());
        CompletableFuture.runAsync(() -> {
        	try {
				checkUnitJobService
					.prepareJobs(arrangementJob)
					.forEach(this::send);
				//Если получили сообщение на перезапуск, нужно поднять сервис
				if(arrangementJob.getRunType().equals(ArrangementJob.JobRunType.RESTART)){
					sendStartExecutorsMessage(arrangementJob.getAccessToolUnit());
				}
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
					log.info("Отправлено задание на проверку запрещенного ресурса: " + result.getProducerRecord().value().toString());
				}

				@Override
				public void onFailure(Throwable ex) {
					throw new AS_15_8_DispatcherException("Ошибка при отправке задания на проверку", ex);
				}
			});
		} catch (Exception ex) {
			throw new AS_15_8_DispatcherException("Ошибка при отправке задания на проверку", ex);
		}
	}

	private void sendStartExecutorsMessage(AccessToolUnit accessToolUnit) {
		try {
			ExecutorControlMessage controlMessage = new ExecutorControlMessage(accessToolUnit, ExecutorControlMessage.ControlCommand.START);

			Message<ExecutorControlMessage> message = MessageBuilder
					.withPayload(controlMessage)
					.setHeader(KafkaHeaders.TOPIC, controlTopicName)
					.build();

			ListenableFuture<SendResult<String, ExecutorControlMessage>> future = controlMessagesTemplate.send(message);

			future.addCallback(new ListenableFutureCallback<SendResult<String, ExecutorControlMessage>>() {

				@Override
				public void onSuccess(SendResult<String, ExecutorControlMessage> result) {
					log.info("Сообщение для запуска модулей выполнения проверок успешно отправлено");
				}
				@Override
				public void onFailure(Throwable ex) {
					throw new RuntimeException("Ошибка при отправке сообщения для запсука модулей выполнения проверок", ex);
				}
			});
			future.get();
		} catch (Exception ex) {
			throw new RuntimeException("Ошибка при отправке сообщения для запуска модулей выполнения проверок", ex);
		}
	}
	
}
