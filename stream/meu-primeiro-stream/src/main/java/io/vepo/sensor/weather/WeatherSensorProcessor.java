package io.vepo.sensor.weather;

import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeatherSensorProcessor implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(WeatherSensorProcessor.class);

    private KafkaStreams streams;
    private CountDownLatch latch;

    public WeatherSensorProcessor() {
        streams = null;
        latch = null;
    }   

    @Override
    public void close() {
        if(Objects.nonNull(streams)) {
            latch.countDown();
            streams.close();
        }
    }

    Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();
        builder.addStateStore(Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore("weather-memory"),
                                                          new GeolocationSerde(),
                                                          new WeatherInfoSerde()))
               .stream("weather", Consumed.with(new GeolocationSerde(), new WeatherInfoSerde()))
               .process(new ProcessorSupplier<Geolocation, WeatherInfo, Geolocation, WeatherInfo>() {

                   @Override
                   public Processor<Geolocation, WeatherInfo, Geolocation, WeatherInfo> get() {
                       return new Processor<Geolocation, WeatherInfo, Geolocation, WeatherInfo>() {
                           private KeyValueStore<Geolocation, WeatherInfo> store;
                           private ProcessorContext<Geolocation, WeatherInfo> context;

                           @Override
                           public void init(ProcessorContext<Geolocation, WeatherInfo> context) {
                               this.context = context;
                               this.store = context.getStateStore("weather-memory");
                           }

                           @Override
                           public void process(Record<Geolocation, WeatherInfo> record) {
                               WeatherInfo previousValue = store.get(record.key());
                               if (Objects.nonNull(previousValue)) {
                                   double deltaTimestamp = (record.timestamp() - previousValue.getTimestamp()) / TimeUnit.MINUTES.toMillis(1);

                                   if (deltaTimestamp > 0.0) {
                                       double deltaTemperatura = previousValue.getTemperature() - record.value().getTemperature();
                                       if (Math.abs(deltaTemperatura) / deltaTimestamp > 1.0) {
                                           logger.warn("[BEFORE] Fixing temperature! delta={}", deltaTemperatura);
                                           deltaTemperatura = (deltaTemperatura > 0 ? 1.0 : -1.0) * deltaTimestamp;
                                           logger.warn("[AFTER ] Fixing temperature! delta={}", deltaTemperatura);
                                       }
    
                                       WeatherInfo stableValue = new WeatherInfo(record.key(),
                                                                                 deltaTemperatura + previousValue.getTemperature(),
                                                                                 record.value().getWind(),
                                                                                 record.value().getTimestamp());
                                       store.put(record.key(), stableValue);
                                       context.forward(record.withValue(stableValue));
                                   } else {
                                       logger.warn("Too close temperature. delta={} ignored={}", deltaTimestamp, record);
                                   }
                               } else {
                                   store.put(record.key(), record.value());
                                   context.forward(record);
                               }
                           }
                       };
                   }

               }, "weather-memory")
               .to("weather-stable");
        return builder.build();
    }

    public void start(Properties props) {        
        streams = new KafkaStreams(buildTopology(), props);
        latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "weather-sanity");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, JsonSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class);
        try (WeatherSensorProcessor processor = new WeatherSensorProcessor()) {
            processor.start(props);
        }
    }
}
