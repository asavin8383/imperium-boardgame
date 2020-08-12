package common;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import services.ActService;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={SendCheckUnitToKafka.TestConfig.class, ApplicationConfiguration.class})
@PropertySource("classpath:application.yml")
public class SendCheckUnitToKafka {

	@Autowired
    private KafkaTemplate<String, CheckUnitJob> testKafkaTemplate;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ActService actService;

	@Value("${spring.cloud.stream.bindings.inputJobs.destination}")
    private String topic;
	
	@Test
	public void test() {

		String sql = "select 'URL' as check_unit_type, url.url as check_unit_value\n" +
				"from sa.content\n" +
				"         join sa.url on content.id = url.content_id and content.id IN (select content.id from sa.content limit 1000) and blocktype is null";

		jdbcTemplate.queryForList(sql)
			.forEach(result -> {
				CheckUnitJob checkUnitJob = new CheckUnitJob();
				checkUnitJob.setAccessTool("hola");

				checkUnitJob.setCheckUnit(new CheckUnit(
						1L,
						CheckUnitType.valueOf(result.get("check_unit_type").toString()),
						result.get("check_unit_value").toString()
					)
				);

				/*checkUnitJob.getAccessToolParameters().put(AccessToolParameter.PROXY_DNS_NAME, "192.168.5.194");
				checkUnitJob.getAccessToolParameters().put(AccessToolParameter.PROXY_PORT, "3128");*/

				Message<CheckUnitJob> message = MessageBuilder
						.withPayload(checkUnitJob)
						.setHeader(KafkaHeaders.TOPIC, topic)
						.build();

				ListenableFuture<SendResult<String, CheckUnitJob>> future = testKafkaTemplate.send(message);

				future.addCallback(new ListenableFutureCallback<SendResult<String, CheckUnitJob>>() {

					@Override
					public void onSuccess(SendResult<String, CheckUnitJob> result) {
						System.out.println("Сообщение успешно отправлено: arrangenmentID " + result.getProducerRecord().value().toString());
					}

					@Override
					public void onFailure(Throwable ex) {
						System.out.println("Ошибка при отправке сообщения: " + ex.getMessage());
					}
				});
			});
	}

	@Test
	public void testNLP() {

		System.out.println("-------------------------------");

		String sql = "select 'URL' as check_unit_type, res.value as check_unit_value \n" +
				"from sor.content_resources res \n" +
				"where resource_type_id = 6 \n" +
				"limit 10 offset 10";

		jdbcTemplate.queryForList(sql)
				.forEach(result -> {
					CheckUnitJob checkUnitJob = new CheckUnitJob();
					checkUnitJob.setAccessTool("express");

					checkUnitJob.setCheckUnit(new CheckUnit(
					 1L,
							CheckUnitType.valueOf(result.get("check_unit_type").toString()),
							result.get("check_unit_value").toString()
						)
					);

					Message<CheckUnitJob> message = MessageBuilder
							.withPayload(checkUnitJob)
							.setHeader(KafkaHeaders.TOPIC, topic)
							.build();

					ListenableFuture<SendResult<String, CheckUnitJob>> future = testKafkaTemplate.send(message);

					future.addCallback(new ListenableFutureCallback<SendResult<String, CheckUnitJob>>() {

						@Override
						public void onSuccess(SendResult<String, CheckUnitJob> result) {
							System.out.println("Сообщение успешно отправлено: arrangenmentID " + result.getProducerRecord().value().toString());
						}

						@Override
						public void onFailure(Throwable ex) {
							System.out.println("Ошибка при отправке сообщения: " + ex.getMessage());
						}
					});
				});
	}

	@Test
	public void testAct() {

		System.out.println("BEGIN -------------------------------");

		actService.createManualAct(1000L, "testOpName");
		//actService.createAct(666L);

		System.out.println("END -------------------------------");

	}

	@Configuration
	static class TestConfig {

		@Value("${spring.cloud.stream.kafka.binder.brokers}")
		private String bootstrapServers;

		@Bean
		Map<String, Object> producerFactoryConfig(){
			Map<String, Object> configProps = new HashMap<>();
			configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
			configProps.put(ProducerConfig.ACKS_CONFIG, "all");
			configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
			configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
			return configProps;
		}

		@Bean
		public ProducerFactory<String, CheckUnitJob> testProducerFactory() {
			return new DefaultKafkaProducerFactory<>(producerFactoryConfig());
		}

		@Bean
		public KafkaTemplate<String, CheckUnitJob> testKafkaTemplate() {
			return new KafkaTemplate<>(testProducerFactory());
		}
	}
}

