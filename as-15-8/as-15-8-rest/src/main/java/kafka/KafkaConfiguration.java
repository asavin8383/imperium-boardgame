package kafka;

import arrangement.ArrangementStatusNotification;
import jobs.ArrangementJob;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 */
@EnableKafka
@Configuration
public class KafkaConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.group}")
    private String group;

    @Value("${spring.kafka.auto-offset-reset}")
    private String offset;

    //**********************Отправка диспетчеру**********************
    @Bean
    public ProducerFactory<String, ArrangementJob> arrangementJobProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    @Bean
    public KafkaTemplate<String, ArrangementJob> arrangementJobKafkaTemplate() {
        return new KafkaTemplate<>(arrangementJobProducerFactory());
    }

    //***************************************************************

    //**********************Прием от диспетчера**********************
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
    public ConsumerFactory<String, ArrangementStatusNotification> arrangementStatusNotificationConsumerFactory(){
        return new DefaultKafkaConsumerFactory<>(
                consumerFactoryConfig(),
                new StringDeserializer(),
                new JsonDeserializer<>(ArrangementStatusNotification.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ArrangementStatusNotification> kafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String, ArrangementStatusNotification> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(arrangementStatusNotificationConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
    //***************************************************************

}
