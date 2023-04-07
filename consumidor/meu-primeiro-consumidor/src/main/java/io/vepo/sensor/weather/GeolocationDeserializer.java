package io.vepo.sensor.weather;

import java.nio.ByteBuffer;

import org.apache.kafka.common.serialization.Deserializer;

public class GeolocationDeserializer implements Deserializer<Geolocation> {

    @Override
    public Geolocation deserialize(String topic, byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        double lat = buffer.getDouble();
        double lon = buffer.getDouble();
        return new Geolocation(lat, lon);
    }
}
