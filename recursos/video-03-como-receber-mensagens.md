# Kafka do ZERO: Como receber mensagens?

## Pré-requisitos

* Broker Kafka em execução
* Tópico criado
    - É importante que o Tópico exista antes de você iniciar seu consumer
    - Caso você apague o tópico, reinicialize seu consumer
* Mensagens sejam enviadas ao tópico

## Responsabilidades do Consumer
- Deserializar
- Definir partições a serem consumidas
- Balanceamento de carga
    > partition.assignment.strategy

## Criando Consumer

Similar ao Kafka Producer, mas usando Deserializer e não serializer

- consumidor\meu-primeiro-consumidor\src\main\java\io\vepo\sensor\weather\GeolocationDeserializer.java

* Ver Consumer Configs https://kafka.apache.org/documentation/#consumerconfigs
    * Configurações importantes
        - group.id
        - auto.offset.reset

### Consumer Simples (_at-least-once_)
```java
Properties configs = new Properties();
configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
configs.put(ConsumerConfig.GROUP_ID_CONFIG, "meu-consumer");
configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, GeolocationDeserializer.class);
configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, WeatherInfodDeserializer.class);
configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // earliest, latest ou none 
                                                                  // none throw exception to the consumer if no previous offset is found for the consumer's group
AtomicBoolean runing = new AtomicBoolean(true);
CountDownLatch latch = new CountDownLatch(1);
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
try(Consumer<Chave, Mensagem> consumer = new KafkaConsumer<>(configs)) {
    consumer.subscribe(Arrays.asList("meu-topic"));
    while(running.get()) {
        ConsumerRecords<Chave, Mensagem> records = consumer.poll(Duration.ofMillis(1000));
        records.forEach(this::consume);
    }
}
latch.countDown();
```
* A primeira mensagem que o consumer irá consumir será definida por `auto.offset.reset` que será usado somente na primeira execução
* Cada chamada do método `poll` não poderá exceder [`session.timeout.ms`](https://kafka.apache.org/documentation/#consumerconfigs_session.timeout.ms)
* É recomendável que seja em média o valor de [`heartbeat.interval.ms`](https://kafka.apache.org/documentation/#consumerconfigs_heartbeat.interval.ms)
* Os offsets são commitados a cada 5s (auto.commit.interval.ms=5000) ver [`auto.commit.interval.ms`](https://kafka.apache.org/documentation/#consumerconfigs_auto.commit.interval.ms)
* Caso o consumo das mensagens demore muito, esse KafkaConsumer será descartado do cluster que executará uma operação de Rebalancing
* Irá consumir qualquer mensagem enviada, mesmo as mensagens enviadas por transações não finalizadas. Ver propriedade [`isolation.level`](https://kafka.apache.org/documentation/
#consumerconfigs_isolation.level)
* Modo de operação padrão _at-least-once_

### Consumer _Exactly-Once_

```java
Properties configs = new Properties();
configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
configs.put(ConsumerConfig.GROUP_ID_CONFIG, "meu-consumer");
configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, GeolocationDeserializer.class);
configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, WeatherInfodDeserializer.class);
configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); 
configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
configs.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
AtomicBoolean runing = new AtomicBoolean(true);
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
try(Consumer<Chave, Mensagem> consumer = new KafkaConsumer<>(configs)) {
    consumer.subscribe(Arrays.asList("meu-topic"));
    while(running.get()) {
        ConsumerRecords<Chave, Mensagem> records = consumer.poll(Duration.ofMillis(1000));
        for (TopicPartition partition : records.partitions()) {
            List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
            for (ConsumerRecord<String, String> record : partitionRecords) {
                consume(record);
            }
            long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
            // commit deve marcar a posição a começar a leitura
            // por isso sempre offset + 1
            consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)));
        }
    }
}
latch.countDown();
```

## Operação de Rebalancing

* Triggers
    - Novo consumer iniciado
    - Consumer finalizado
    - Consumer com timeout de sessão
* Executa balanceamento de carga entre os consumers
* Distribuição das partições entre os brokers
* Pode haver perda de dados
* _Stop-the-world_


```
              t0                                 t2
C1      ---------------------(poll)----------------------------------->
               | (heartbeat)                     | (heartbeat)
C2      ------------------------------(poll)-------------------------->
               |                                 |
C3      -------------------------------------------------------------->
               |                                 |
               v                                 v
Broker  -------------------------------------------------------------->
```

## Modelo de Threads

* Consumer vai consumir por partição
* Mensagens com a mesma chave são salvas na mesma partição
* Use um KafkaConsumer por Thread para facilitar os commits
