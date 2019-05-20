package kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import execution.ExecutionJobResult;

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
    public ConsumerFactory<String, ExecutionJobResult> executionResultsConsumerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        //config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        return new DefaultKafkaConsumerFactory<>(
        	config,
        	new StringDeserializer(),
            new JsonDeserializer<>(ExecutionJobResult.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ExecutionJobResult> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ExecutionJobResult> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(executionResultsConsumerFactory());
       // factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}
