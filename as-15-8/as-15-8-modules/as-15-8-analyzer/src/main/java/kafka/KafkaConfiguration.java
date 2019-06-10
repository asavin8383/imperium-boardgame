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
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import analysis.AnalysisResult;
import checkUnits.CheckUnitStatusNotification;
import execution.ExecutionJobResult;

@EnableKafka
@Configuration
public class KafkaConfiguration {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

    @Value("${spring.kafka.auto-offset-reset}")
    private String autoOffsetReset;
    
    @Value("${spring.kafka.analysis-concurrency}")
    private Integer analysisConcurrency;
	
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
    Map<String, Object> consumerFactoryConfig(){
    	 Map<String, Object> config = new HashMap<>();
         config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
         config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
         config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
         config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
         config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
         return config;
    }
    
    @Bean
    public ConsumerFactory<String, ExecutionJobResult> executionResultsConsumerFactory() {      
        return new DefaultKafkaConsumerFactory<>(
        	consumerFactoryConfig(),
        	new StringDeserializer(),
            new JsonDeserializer<>(ExecutionJobResult.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ExecutionJobResult> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ExecutionJobResult> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(executionResultsConsumerFactory());
        factory.setConcurrency(analysisConcurrency);
        return factory;
    }
    
    @Bean
    public ProducerFactory<String, AnalysisResult> analysisResultProducerFactory() {	
    	return new DefaultKafkaProducerFactory<>(producerFactoryConfig());
    }
    
    @Bean
    public ProducerFactory<String, CheckUnitStatusNotification> notificationsProducerFactory() {
    	return new DefaultKafkaProducerFactory<>(producerFactoryConfig());
    }
    
    @Bean
    public KafkaTemplate<String, AnalysisResult> analysisResultTemplate() {
        return new KafkaTemplate<>(analysisResultProducerFactory());
    }
    
    @Bean
    public KafkaTemplate<String, CheckUnitStatusNotification> notificationsTemplate() {
        return new KafkaTemplate<>(notificationsProducerFactory());
    }
}
