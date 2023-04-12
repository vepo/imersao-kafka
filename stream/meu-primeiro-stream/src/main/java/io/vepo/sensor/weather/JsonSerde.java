package io.vepo.sensor.weather;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerde<T> implements Serde<T> {

    private final ObjectMapper mapper;
    private final Map<String, Class<?>> topicPojoClass;

    public JsonSerde() {
        mapper = new ObjectMapper();
        topicPojoClass = new HashMap<>();
    }

    @Override
    public Serializer<T> serializer() {
        return new Serializer<T>() {

            @Override
            public byte[] serialize(String topic, T data) {
                try {
                    return mapper.writeValueAsString(data).getBytes();
                } catch (JsonProcessingException e) {
                    throw new KafkaException("Não foi possível serializar objeto!", e);
                }
            }

        };
    }

    @Override
    public Deserializer<T> deserializer() {
        return new Deserializer<T>() {

            @SuppressWarnings("unchecked")
            @Override
            public T deserialize(String topic, byte[] data) {
                try {
                    return (T) mapper.readValue(data, topicPojoClass.get(topic));
                } catch (IOException e) {
                    throw new KafkaException("Não foi possível deserializar objeto!", e);
                }
            }

        };
    }

}
