package io.vepo.sensor.weather;

import java.nio.ByteBuffer;

import org.apache.kafka.common.serialization.Deserializer;

public class WeatherInfoDeserializer implements Deserializer<WeatherInfo> {

    @Override
    public WeatherInfo deserialize(String topic, byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        double lat = buffer.getDouble();
        double lon = buffer.getDouble();
        double temperature = buffer.getDouble();
        double wind = buffer.getDouble();
        long timestamp = buffer.getLong();
        return new WeatherInfo(new Geolocation(lat, lon), temperature, wind, timestamp);
    }
}
