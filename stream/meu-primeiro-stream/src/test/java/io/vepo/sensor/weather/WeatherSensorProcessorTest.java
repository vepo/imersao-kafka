package io.vepo.sensor.weather;

import java.util.Properties;

import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.Test;

public class WeatherSensorProcessorTest {

    @Test
    void setupTest() {

        try (WeatherSensorProcessor processor = new WeatherSensorProcessor()) {
            Topology topology = processor.buildTopology();

            // setup test driver
            Properties props = new Properties();
            props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
            props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
            props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, JsonSerde.class);
            props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class);
            try (TopologyTestDriver testDriver = new TopologyTestDriver(topology, props)) {
                // test
            }
        }
    }
}
