package io.vepo.sensor.weather;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class WeatherInfoSerde implements Serde<WeatherInfo> {

    @Override
    public Serializer<WeatherInfo> serializer() {
        return new WeatherInfoSerializer();
    }

    @Override
    public Deserializer<WeatherInfo> deserializer() {
        return new WeatherInfoDeserializer();
    }

}
