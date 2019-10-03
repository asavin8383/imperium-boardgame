package kafka;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import control.ExecutorControlMessage;
import enums.AccessToolParameters;
import enums.AccessToolUnit;
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

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SendJobsToKafka.TestConfig.class)
@PropertySource("classpath:application.yml")
public class SendJobsToKafka {

    @Autowired
    private KafkaTemplate<String, CheckUnitJob> kafkaTemplate;

    @Value("${spring.cloud.stream.bindings.jobs.destination}")
    private String topicName;

    @Test
    public void test() {

        for(long i = 10000; i<10010; i++) {
            CheckUnitJob checkUnitJob = new CheckUnitJob();
            checkUnitJob.setJobID(1L);
            checkUnitJob.setAccessToolUnit(AccessToolUnit.GOOGLE);

            checkUnitJob.setCheckUnit(new CheckUnit(CheckUnitType.URL, "https://www.google.ru"));

            checkUnitJob.getAccessToolParameters().put(AccessToolParameters.SEARCH_SYSTEM_URL, "https://www.google.ru");
            checkUnitJob.getAccessToolParameters().put(AccessToolParameters.SEARCH_SYSTEM_XPATH_INPUT_FIELD, "//input[@name=\"q\"]");
            checkUnitJob.getAccessToolParameters().put(AccessToolParameters.SEARCH_SYSTEM_XPATH_CAPTCHA, "//form[@id=\"captcha-form\"]");
            checkUnitJob.getAccessToolParameters().put(AccessToolParameters.SEARCH_SYSTEM_XPATH_NEXT_PAGE, "//*[@id=\"pnnext\"]");
            checkUnitJob.getAccessToolParameters().put(AccessToolParameters.SEARCH_SYSTEM_XPATH_ITEM_LINK, "//div[@class=\"g\"]//div[@class=\"r\"]/a[1]");
            checkUnitJob.getAccessToolParameters().put(AccessToolParameters.INPUT_DELAY, "0");
            checkUnitJob.getAccessToolParameters().put(AccessToolParameters.SEARCH_SYSTEM_RESULT_PAGE_TYPE, "pagination");

            Message<CheckUnitJob> message = MessageBuilder
                    .withPayload(checkUnitJob)
                    .setHeader(KafkaHeaders.TOPIC, topicName)
                    .build();

            ListenableFuture<SendResult<String, CheckUnitJob>> future = kafkaTemplate.send(message);

            future.addCallback(new ListenableFutureCallback<SendResult<String, CheckUnitJob>>() {

                @Override
                public void onSuccess(SendResult<String, CheckUnitJob> result) {
                    System.out.println("Сообщение успешно отправлено: "+ result.getProducerRecord().value().toString());
                }
                @Override
                public void onFailure(Throwable ex) {
                    System.out.println("Ошибка при отправке сообщения: " + ex.getMessage());
                }
            });
        }
    }

    @Configuration
    static class TestConfig {

        @Value("${spring.cloud.stream.kafka.binder.brokers}")
        private String bootstrapServers;

        @Bean
        Map<String, Object> producerFactoryConfig() {
            Map<String, Object> configProps = new HashMap<>();
            configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            configProps.put(ProducerConfig.ACKS_CONFIG, "all");
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            return configProps;
        }

        @Bean
        public ProducerFactory<String, CheckUnitJob> producerFactory() {
            return new DefaultKafkaProducerFactory<>(producerFactoryConfig());
        }

        @Bean
        public ProducerFactory<String, ExecutorControlMessage> controlMessagesProducerFactory() {
            return new DefaultKafkaProducerFactory<>(producerFactoryConfig());
        }

        @Bean
        public KafkaTemplate<String, CheckUnitJob> kafkaTemplate() {
            return new KafkaTemplate<>(producerFactory());
        }

        @Bean
        public KafkaTemplate<String, ExecutorControlMessage> controlMessagesTemplate() {
            return new KafkaTemplate<>(controlMessagesProducerFactory());
        }
    }
}
