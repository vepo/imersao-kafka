package io.vepo.sensor.weather;

import java.nio.ByteBuffer;

import org.apache.kafka.common.serialization.Serializer;

public class WeatherInfoSerializer implements Serializer<WeatherInfo> {

    @Override
    public byte[] serialize(String topic, WeatherInfo data) {
        return ByteBuffer.allocate(Double.SIZE * 4 + Long.SIZE)
                .putDouble(data.getLocation().getLat())
                .putDouble(data.getLocation().getLon())
                .putDouble(data.getTemperature())
                .putDouble(data.getWind())
                .putLong(data.getTimestamp())
                .array();
    }

}
