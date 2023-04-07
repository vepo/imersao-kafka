package io.vepo.sensor.weather;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeatherSensorConsumer implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(WeatherSensorConsumer.class);
    private Consumer<Geolocation, WeatherInfo> consumer;
    private CountDownLatch latch;
    private AtomicBoolean running;

    private Map<Geolocation, WeatherAggregate> cache;

    public WeatherSensorConsumer(Consumer<Geolocation, WeatherInfo> consumer) {
        this.consumer = consumer;
        this.latch = new CountDownLatch(1);
        this.running = new AtomicBoolean(true);
        this.cache = new HashMap<>();
    }

    @Override
    public void close() {
        consumer.close();
    }

    public void join() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void consume() {
        consumer.subscribe(Arrays.asList("weather"));
        while (running.get()) {
            consumer.poll(Duration.ofSeconds(1))
                    .forEach(this::consumeWeatherInfo);
        }
        latch.countDown();
    }

    public void start() {
        Runtime.getRuntime()
               .addShutdownHook(new Thread() {
                   @Override
                   public void run() {
                       running.set(false);
                       try {
                           latch.await();
                       } catch (InterruptedException e) {
                           Thread.currentThread().interrupt();
                       }
                   }
               });

        Executors.newSingleThreadExecutor()
                 .submit(this::consume);
    }

    private void consumeWeatherInfo(ConsumerRecord<Geolocation, WeatherInfo> weatherInfo) {
        logger.info("Processando valor! mensagem={}", weatherInfo);
        if (Objects.isNull(weatherInfo.key())) {
            logger.warn("Chave nula! Ignorando registro! {}", weatherInfo);
        } else {
            WeatherAggregate aggre = cache.compute(weatherInfo.key(), 
                                                   (key, value) -> {
                                                       if (Objects.isNull(value)) {
                                                           return new WeatherAggregate(weatherInfo.value().getTemperature(),
                                                                                       weatherInfo.value().getTemperature(),
                                                                                       weatherInfo.value().getWind(),
                                                                                       weatherInfo.value().getWind());
                                                       } else {
                                                           return new WeatherAggregate(Math.max(value.getMaxTemperature(), weatherInfo.value().getTemperature()),
                                                                                       Math.min(value.getMinTemperature(), weatherInfo.value().getTemperature()),
                                                                                       Math.max(value.getMaxWind(), weatherInfo.value().getWind()),
                                                                                       Math.min(value.getMinWind(), weatherInfo.value().getWind()));
                                                       }
                                                   });
            logger.info("Valor: {}", aggre);
        }
    }

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "meu-primeiro-consumidor");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, GeolocationDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, WeatherInfoDeserializer.class);
        try (WeatherSensorConsumer consumer = new WeatherSensorConsumer(new KafkaConsumer<>(properties))) {
            consumer.start();
            consumer.join();
        }
    }

}