# Kafka do ZERO: Como enviar mensagens?

## Como configurar um broker

### Localmente
Passo a passo: https://github.com/vepo/imersao-kafka/blob/main/01-configurar-broker-kafka.md

### Docker

Image: https://hub.docker.com/r/vepo/kafka


Docker Compose:

```yaml
version: '3'
services:
  kafka-0:
    image: vepo/kafka:3.7.0
    container_name: kafka-0
    ports:
     - 9092:9092
     - 9093:9093
    environment:
      KAFKA_CLUSTER_ID: N0UOwvnVR7ORiwq0M4do9w
      KAFKA_BROKER_ID: 1
      KAFKA_NODE_ID: 1
      KAFKA_NUM_PARTITIONS: 3
      KAFKA_LISTENERS: PLAINTEXT://:9092,CONTROLLER://kafka-0:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-0:9092
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka-0:9093,2@kafka-1:9095,3@kafka-2:9097
  kafka-1:
    image: vepo/kafka:3.7.0
    container_name: kafka-1
    ports:
     - 9094:9094
     - 9095:9095
    environment:
      KAFKA_CLUSTER_ID: N0UOwvnVR7ORiwq0M4do9w
      KAFKA_BROKER_ID: 2
      KAFKA_NODE_ID: 2
      KAFKA_NUM_PARTITIONS: 3
      KAFKA_LISTENERS: PLAINTEXT://:9094,CONTROLLER://kafka-1:9095
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-1:9094
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka-0:9093,2@kafka-1:9095,3@kafka-2:9097
  kafka-2:
    image: vepo/kafka:3.7.0
    container_name: kafka-2
    ports:
     - 9096:9096
     - 9097:9097
    environment:
      KAFKA_CLUSTER_ID: N0UOwvnVR7ORiwq0M4do9w
      KAFKA_BROKER_ID: 3
      KAFKA_NODE_ID: 3
      KAFKA_NUM_PARTITIONS: 3
      KAFKA_LISTENERS: PLAINTEXT://:9096,CONTROLLER://kafka-2:9097
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-2:9096
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka-0:9093,2@kafka-1:9095,3@kafka-2:9097
``` 

#### Configurar hosts
**Importante**: É preciso configurar o arquivos hosts para uma aplicação acessar o broker dentro de um container docker.

* Linux:   `/etc/hosts`
* Windows: `C:\Windows\System32\Drivers\etc\hosts`

> 127.0.0.1 kafka-0 kafka-1 kafka-2

## Como é uma mensagem Kafka?

* Chave     → Particionamento
* Registro  → Dado
* Headers   → Metadados do Cliente (Producer/Consumer)
* Metadados → Metadados do Broker
    * Timestamp 
    * Offset
    * Partição
    * Tópico

Chave (_key_) e Registro (_record_) são para o broker sempre dados binários. 

* Não há indices
* O broker não faz qualquer suposição acerca do conteúdo (sumário, identificação de campos, schema, etc..)
* Partição/Tópico/Timestamp são definidos pelo Producer
* Offset é sempre definido pelo Broker

Interessante entender que não há correlação de ordenação entre timestamp e ordenação.
Importante ler → Relógios Físicos e Lógicos https://blog.vepo.dev/posts/relogios-fisicos-e-logicos
Lamport Clock pode ser definido por Partição/Offset e é a definição de ordenação

## O Kafka Producer

Configurações:
  - https://kafka.apache.org/documentation/#producerconfigs

Entendendo o "bootstrap.servers"
  - Producer vai iniciar a conexão com algum broker listado
  - Producer vai escolher qual broker se reconectar usando o "advertised.listeners"
    - https://kafka.apache.org/documentation/#brokerconfigs_advertised.listeners

Responsabilidades:

- Serializar Dados e Chaves
  - key.serializer
  - value.serializer
- Definir Partições
  - partitioner.class

Classe de Serialização
  - produtor\meu-primeiro-produtor\src\main\java\io\vepo\sensor\weather\GeolocationSerializer.java

Particionamento
  - https://cwiki.apache.org/confluence/display/KAFKA/KIP-794%3A+Strictly+Uniform+Sticky+Partitioner#KIP794:StrictlyUniformStickyPartitioner-Status
  - https://kafka.apache.org/37/javadoc/org/apache/kafka/clients/producer/Partitioner.html

### Enviando a mensagem

Ler Javadoc: https://kafka.apache.org/37/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html

1. "Sincronamente"

```java
Properties configs = new Properties();
configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, GeolocationSerializer.class);
configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, WeatherInfoSerializer.class);
try(Producer<Chave, Mensagem> producer = new KafkaProducer<>(configs)) {
  Mensagem mensagem = /*   */
  Future<RecordMetadata> talvezMetadados = producer.send(new ProducerRecord<String, String>("topico", mensagem.getChave(), mensagem));
  producer.flush()
}
```

2. Assincronamente
```java
Properties configs = new Properties();
configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, GeolocationSerializer.class);
configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, WeatherInfoSerializer.class);
try(Producer<Chave, Mensagem> producer = new KafkaProducer<>(configs)) {
  Mensagem mensagem = /*   */
  producer.send(new ProducerRecord<String, String>("topico", mensagem.getChave(), mensagem),
               (metadados, exception) -> {
                 // faça o que desejar
               });
}
```

### Testando sua classe produtora

1. Use Testcontainers
  - https://java.testcontainers.org/modules/kafka/
2. Use MockProducer
  - https://kafka.apache.org/34/javadoc/org/apache/kafka/clients/producer/MockProducer.html