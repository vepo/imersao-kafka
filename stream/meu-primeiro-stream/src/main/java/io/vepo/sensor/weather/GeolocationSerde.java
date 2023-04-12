package io.vepo.sensor.weather;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class GeolocationSerde implements Serde<Geolocation> {

    @Override
    public Serializer<Geolocation> serializer() {
        return new GeolocationSerializer();
    }

    @Override
    public Deserializer<Geolocation> deserializer() {
        return new GeolocationDeserializer();
    }
}
