# Apache Kafka do ZERO: O que é Kafka Stream?

* Para que serve o Kafka Stream?
* Quando usar Kafka Stream?

> Falar de Kafka Stream e Kafka Connect é falar de Data Stream Processing

## Embasamento Teórico
> [Open Access] A Survey of Distributed Data Stream Processing Frameworks
    https://ieeexplore.ieee.org/document/8864052

_Os "Data Stream" (fluxo de dados em tradução livre) são sequências de tuplas ilimitadas geradas continuamente no tempo._

### Uma evolução aos sistemas _Batch_
_Sistemas Batch Processing (processamento em lote) sofrem com problemas de latência devido à necessidade de coletar dados de entrada em lotes antes que eles possam ser processados._

> Evolução: ETL → Batch → Data Stream Processing

### Componentes de uma engine Data Stream Processing

i.   Data stream ingestion layer (Source Layer)
ii.  Data stream processing layer
iii. Storage layer
iv.  Resource management layer
v.   Output layer (Sink Layer)

## Caracterizando o Kafka Stream

i. Ingestion Layer
    - Macro (Kafka Connect)
    - Micro (Kafka)
ii. Processing Layer 
    - Kafka Stream
        - Loop Poll-Process-Punctuate
iii. Storage Layer
    - RocksDB
iv. Resource Management Layer
    - Não existe um mecanismo centralizado
v. Output Layer
    - Macro (Kafka Connect)
    - Micro (Kafka)

## Micro e Macro

* Kafka Stream permite o processamento Stream no nível micro, dentro de um mesmo Kafka Stream, e no nível macro, dentro do cluster Kafka.
* Podemos considerar o Kafka como um grande Data Stream Processing, um grafo de transformação de dados

```
                                +----------+ 
                                |          |
                   +----------> | Tópico B |
                  /             |          |
                 /              +----------+ 
+----------+    /
|          |   /
| Tópico A |--<
|          |   \
+----------+    \
                 \              +----------+ 
                  \             |          |
                   +----------> | Tópico C |
                                |          |
                                +----------+ 
```


## Kafka Stream

* Loop: Poll, Process, Punctuate
* Funciona de forma similar ao Consumer/Producer
* É composto por Consumer, Producer e uma isntância do RocksDB
* StreamsBuilder
    - https://kafka.apache.org/37/javadoc/org/apache/kafka/streams/StreamsBuilder.html
* Elementos
    - Stream:      Processa dados
    - State Store: Key Value store para ser usado nos jobs de processamento
    - KTable:      Expões um tópico como uma tabela

```java
Properties props = new Properties();
props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(StreamsConfig.APPLICATION_ID_CONFIG, "weather-sanity");
props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, JsonSerde.class);
props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 10);
props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class);

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
                        context.schedule(Duration.ofMinutes(10), PunctuationType.WALL_CLOCK_TIME, this:cleanup);
                    }

                    private void cleanup(long timestamp) {
                        this.store().all().forEachRemaining(pair -> this.store().delete(pair.key));
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

KafkaStreams streams = new KafkaStreams(builbuilder.build(), props);
CountDownLatch latch = new CountDownLatch(1);

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
```
### Kafka Stream DSL

Ver classe KStream 
    - https://kafka.apache.org/37/javadoc/org/apache/kafka/streams/kstream/KStream.html

### State Store

Local vs Global 

Local:  Uma thread acessa um conjunto de partições
Global: Uma thread acessa todas as partições

