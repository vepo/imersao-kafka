package io.vepo.sensor.weather;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class WeatherSensorCollector implements AutoCloseable {

    private final Producer<Geolocation, WeatherInfo> producer;

    public WeatherSensorCollector(Producer<Geolocation, WeatherInfo> producer) {
        this.producer = producer;
    }

    public Future<RecordMetadata> send(WeatherInfo info) {
        return producer.send(new ProducerRecord<Geolocation, WeatherInfo>("weather", info.getLocation(), info));
    }

    @Override
    public void close() {
        this.producer.close();
    }

    public static void main(String[] args) {

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean running = new AtomicBoolean(true);
        Runtime.getRuntime()
               .addShutdownHook(new Thread("shutdown-hook") {
                   @Override
                   public void run() {
                       System.out.println("Shutdown requested!");
                       running.set(false);
                       try {
                           latch.await();
                       } catch (InterruptedException ie) {
                           Thread.currentThread().interrupt();
                       }
                   }
               });
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, GeolocationSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, WeatherInfoSerializer.class);
        try (WeatherSensorCollector producer = new WeatherSensorCollector(new KafkaProducer<>(properties))) {
            while (running.get()) {
                try {
                    System.out.println("Sending message...");
                    WeatherInfo info = random();
                    RecordMetadata metadata = producer.send(info).get();
                    System.out.println("Message metadata: key=" + info.getLocation() + "\n" +
                            "                  value=" + info + "\n" +
                            "                  topic=" + metadata.topic() + "\n" +
                            "                  partition=" + metadata.partition() + "\n" +
                            "                  offset=" + metadata.offset() + "\n" +
                            "                  timestamp=" + metadata.timestamp());
                    Thread.sleep(1_000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ee) {
                    System.err.println("Error: " + ee.getMessage());
                    ee.printStackTrace();
                }
            }
        }
        System.out.println("Done");
        latch.countDown();
    }

    private static Random rnd = new Random();

    private static WeatherInfo random() {
        return new WeatherInfo(new Geolocation(-9.0535783, -40.198365),
                               25.0 + 15.0 * rnd.nextDouble(),
                               100.0 * rnd.nextDouble(),
                               System.currentTimeMillis());
    }
}