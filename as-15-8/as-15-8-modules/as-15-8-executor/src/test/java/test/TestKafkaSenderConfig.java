package test;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import checkUnits.CheckUnitJob;
import control.ExecutorControlMessage;

@Configuration
public class TestKafkaSenderConfig {
 
	@Value("${spring.kafka.bootstrap-servers}")
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