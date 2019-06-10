package kafka;

import java.util.HashMap;
import java.util.Map;

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

import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitStatusNotification;
import control.ExecutorControlMessage;
import execution.ExecutionJobResult;

@EnableKafka
@Configuration
public class KafkaConfiguration {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Value("${spring.kafka.group}")
    private String group;

    @Value("${spring.kafka.auto-offset-reset}")
    private String autoOffsetReset;
    
    @Value("${spring.kafka.executor-concurrency}")
    private Integer listenersConcurrency;
    
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
    
    @Bean 
    Map<String, Object> cousumerFactoryConfig(){
    	Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return config;
    }
    
    @Bean
    public ProducerFactory<String, ExecutionJobResult> executionResultProducerFactory() {
    	return new DefaultKafkaProducerFactory<>(producerFactoryConfig());
    }
    
    @Bean
    public ProducerFactory<String, CheckUnitStatusNotification> notificationsProducerFactory() {
    	return new DefaultKafkaProducerFactory<>(producerFactoryConfig());
    }
    
    @Bean
    public ProducerFactory<String, ExecutorControlMessage> controlMessagesProducerFactory() {
    	return new DefaultKafkaProducerFactory<>(producerFactoryConfig());
    }

    @Bean
    public ConsumerFactory<String, CheckUnitJob> checkUnitJobsConsumerFactory(){
        return new DefaultKafkaConsumerFactory<>(
            	cousumerFactoryConfig(),
            	new StringDeserializer(),
                new JsonDeserializer<>(CheckUnitJob.class)
            );
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CheckUnitJob> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CheckUnitJob> listenerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        listenerFactory.setConsumerFactory(checkUnitJobsConsumerFactory());
        listenerFactory.setConcurrency(listenersConcurrency);
        return listenerFactory;
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ExecutorControlMessage> controlMessagesListenerContainerFactory() {
    	
        ConsumerFactory<String, ExecutorControlMessage> factory = new DefaultKafkaConsumerFactory<>(
        	cousumerFactoryConfig(),
        	new StringDeserializer(),
            new JsonDeserializer<>(ExecutorControlMessage.class)
        );

        ConcurrentKafkaListenerContainerFactory<String, ExecutorControlMessage> listenerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        listenerFactory.setConsumerFactory(factory);
        listenerFactory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);
        listenerFactory.setConcurrency(1);
        return listenerFactory;
    }
 
    @Bean
    public KafkaTemplate<String, ExecutionJobResult> executionResultTemplate() {
        return new KafkaTemplate<>(executionResultProducerFactory());
    }
    
    @Bean
    public KafkaTemplate<String, CheckUnitStatusNotification> notificationsTemplate() {
        return new KafkaTemplate<>(notificationsProducerFactory());
    }
    
    @Bean
    public KafkaTemplate<String, ExecutorControlMessage> controlMessagesTemplate() {
        return new KafkaTemplate<>(controlMessagesProducerFactory());
    }
}
