package kafka;

import java.util.HashMap;
import java.util.Map;

import control.ExecutorControlMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import analysis.AnalysisResult;
import arrangement.ArrangementStatusNotification;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitStatusNotification;
import jobs.ArrangementJob;

@EnableKafka
@Configuration
public class KafkaConfiguration {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Value("${spring.kafka.group}")
    private String group;

    @Value("${spring.kafka.auto-offset-reset}")
    private String offset;

    @Bean
    public Map<String, Object> consumerFactoryConfig(){
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return config;
    }

    @Bean 
    Map<String, Object> producerFactoryConfig(){
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 10000000);
        return configProps;
    }

    //************************Запуск мероприятия************************
    @Bean
    public ConsumerFactory<String, ArrangementJob> arrangementJobConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
        	consumerFactoryConfig(),
        	new StringDeserializer(),
            new JsonDeserializer<>(ArrangementJob.class)
        );
    }
    
    @Bean
    public ProducerFactory<String, CheckUnitJob> checkUnitJobProducerFactory() {
    	return new DefaultKafkaProducerFactory<>(producerFactoryConfig());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ArrangementJob> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ArrangementJob> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(arrangementJobConsumerFactory());
        factory.getContainerProperties().setAckMode(AckMode.MANUAL);
        return factory;
    }
 
    @Bean
    public KafkaTemplate<String, CheckUnitJob> checkUnitJobTemplate() {
        return new KafkaTemplate<>(checkUnitJobProducerFactory());
    }

    //******************************************************************


    //************************Анализ результатов************************
    @Bean
    public ConsumerFactory<String, AnalysisResult> analysisResultsConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
    		consumerFactoryConfig(),
            new StringDeserializer(),
            new JsonDeserializer<>(AnalysisResult.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AnalysisResult> kafkaAnalysisResultListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AnalysisResult> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(analysisResultsConsumerFactory());
        factory.getContainerProperties().setAckMode(AckMode.MANUAL);
        return factory;
    }

    //******************************************************************


    //************************Статус проверки************************

    @Bean
    public ConsumerFactory<String, CheckUnitStatusNotification> jobNotificationsConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
    		consumerFactoryConfig(),
            new StringDeserializer(),
            new JsonDeserializer<>(CheckUnitStatusNotification.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CheckUnitStatusNotification> jobNotificationsListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CheckUnitStatusNotification> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(jobNotificationsConsumerFactory());
        factory.getContainerProperties().setAckMode(AckMode.MANUAL);
        return factory;
    }
    
    //******************************************************************


    //************************Статус мероприятия************************
    
    @Bean
    public ProducerFactory<String, ArrangementStatusNotification> arrangementStatusProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerFactoryConfig());
    }

    @Bean
    public KafkaTemplate<String, ArrangementStatusNotification> arrangementStatusKafkaTemplate() {
        return new KafkaTemplate<>(arrangementStatusProducerFactory());
    }

    //************************Контрольное сообщение на запуск************************

    @Bean
    public ProducerFactory<String, ExecutorControlMessage> controlMessagesProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerFactoryConfig());
    }

    @Bean
    public KafkaTemplate<String, ExecutorControlMessage> controlMessagesTemplate() {
        return new KafkaTemplate<>(controlMessagesProducerFactory());
    }
}
