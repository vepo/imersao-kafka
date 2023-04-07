package io.vepo.sensor.weather;

import java.nio.ByteBuffer;

import org.apache.kafka.common.serialization.Serializer;

public class GeolocationSerializer implements Serializer<Geolocation> {

    @Override
    public byte[] serialize(String topic, Geolocation data) {
        return ByteBuffer.allocate(Double.SIZE * 2)
                .putDouble(data.getLat())
                .putDouble(data.getLon())
                .array();
    }

}
