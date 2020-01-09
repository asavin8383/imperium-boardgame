package events.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Creation date: 18.07.2019
 * Author: asavin
 */
public class SerdesFactory {

    public static <T> Serde<T> createSerde(Class<T> clazz) {
        Map<String, Object> serdeProps = new HashMap<>();
        serdeProps.put("JsonPOJOClass", clazz);

        final Serializer<T> serializer = new POJOSerializer<>();
        serializer.configure(serdeProps, false);

        final Deserializer<T> deserializer = new POJODeserializer<>();
        deserializer.configure(serdeProps, false);

        return Serdes.serdeFrom(serializer, deserializer);
    }
}

class POJOSerializer<T> implements Serializer<T> {

    private ObjectMapper mapper;

    public POJOSerializer() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModules(new Jdk8Module(), new JavaTimeModule());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void configure(Map<String, ?> props, boolean isKey) { }

    @Override
    public byte[] serialize(String topic, T data) {

        if (data == null)
            return null;

        try {
            return mapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new SerializationException("Error serializing " + data, e);
        }
    }

    @Override
    public void close() {
    }

}

class POJODeserializer<T> implements Deserializer<T> {

    private ObjectMapper mapper;

    private Class<T> tClass;

    public POJODeserializer() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModules(new Jdk8Module(), new JavaTimeModule());
    }

    @Override
    public void configure(Map<String, ?> props, boolean isKey) {
        tClass = (Class<T>) props.get("JsonPOJOClass");
    }

    @Override
    public T deserialize(String topic, byte[] bytes) {

        if (bytes == null)
            return null;

        try {
            return mapper.readValue(bytes, tClass);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public void close() {

    }
}