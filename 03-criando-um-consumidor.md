# 3. Como criar um Consumidor de Mensagens

[Voltar](./02-criando-um-produtor.md)

# 3.1. Criando um projeto Java e configurando as bibliotecas necessárias

De forma semelhante ao que foi feito no produtor, crie um projeto e adicione a biblioteca `kafka-clients`.

```bash
mvn archetype:generate -DgroupId=io.vepo.kafka.imersao -DartifactId=meu-primeiro-consumidor -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false
```

```xml
<dependency>4
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>3.4.0</version>
</dependency>
```

# 3.2. Criando a classe consumidora e informando o Maven que ela deve ser executada

Nosso exemplo será um Produtor de informações climáticas, por isso vou criar a classe `WeatherSensorCollector`, ela vai pertencer ao pacote `io.vepo.sensor.weather`, logo deve ser colocada na estrutura abaixo:

```
.
├── src                      ## Todo o código deve ser armazenado nessa pasta
│   ├── main                 ## Código de produção
|   |   ├── java             ## Código Java de produção
|   |   |   └── io
|   |   |       └── vepo
|   |   |           └── sensor
|   |   |               └── weather
|   |   |                   └── WeatherSensorConsumer.java
|   |   └── resources        ## Arquivos que não serão compilados, mas estarão disponíveis em tempo de execução
│   └── test                 ## Código usado para testes unitários
|       ├── java             ## Código Java para testes unitários
|       └── resources        ## Arquivos que não serão compilados, mas estarão disponíveis em tempo de execução
└── pom.xml                  ## Arquivo que define como será feita a build
```

Criada a classe, podemos verificar se está tudo certo executando ela usando o Maven:

```bash
mvn clean compile exec:java -Dexec.mainClass=io.vepo.sensor.weather.WeatherSensorConsumer
```

Para facilitar nossa vida, vamos adicionar definir a classe principal direto no jar e como configuração padrão para execução.

Adicione o seguinte plugin (procure por `build` → `plugins` ou `build` → `pluginManagement` → `plugins`) para configurar a build:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>3.1.0</version>
    <configuration>
        <mainClass>io.vepo.sensor.weather.WeatherSensorCollector</mainClass>
    </configuration>
</plugin>
```

Agora para executar, basta usar:

```bash
mvn clean compile exec:java
```

Para definir que essa classe de execução do jar, altere a configuração do plugin `maven-jar-plugin`:

```xml
<plugin>
    <artifactId>maven-jar-plugin</artifactId>
    <version>3.0.2</version>
    <configuration>
        <archive>
            <manifest>
                <addClasspath>true</addClasspath>
                <mainClass>io.vepo.sensor.weather.WeatherSensorConsumer </mainClass>
            </manifest>
        </archive>
    </configuration>
</plugin>
```

Para executar é preciso primeiro criar o jar:

```bash
mvn clean package
java -jar target/meu-primeiro-consumidor-1.0-SNAPSHOT.jar
```

# 3.3. Implementando o Consumidor

Antes de implementar essa classe, vamos criar alguns pressupostos sobre como ela vai ser utilizada.

1. Todo consumidor Kafka implementa um Loop de pull-process-commit
2. Nossa classe irá consumir dados automaticamente, devemos apenas iniciar uma thread
3. O consumidor irá fazer uma agregação em memória dos dados de temperatura calculando temperatura máxima e mínima e vento máximo e mínimo por dia.
4. A classe já receberá o consumidor inicializado.

```java
WeatherSensorConsumer consumer = new WeatherSensorConsumer(new KafkaConsumer<>(/* inicialização */));
consumer.start();
```

# 3.3.1. Implementando os Deserializers

O primeiro passo quando vamos pensa em um consumidor é escolher nossos Deserializador. Devemos escolher o deserializador dos valores e das chaves. A biblioteca padrão do Kafka já vem com alguns deserializadores, mas eles se resumem a tipos primitivos e objetos simples. O recomendado é que usemos um deserializador open source, como o da Confluent ou da Red Hat, mas para estudo vamos implementar nosso próprio.

Todo deserializador deve implementar a interface [Deserializer](https://kafka.apache.org/34/javadoc/org/apache/kafka/common/serialization/Deserializer.html), [código da biblioteca padrão é aberto](https://github.com/apache/kafka/blob/trunk/clients/src/main/java/org/apache/kafka/common/serialization/Deserializer.java) e podemos ver que são disponibilizado deserializadores para `boolean` ([**BooleanDeserializer**](https://github.com/apache/kafka/blob/trunk/clients/src/main/java/org/apache/kafka/common/serialization/BooleanDeserializer.java)), `byte[]` ([**ByteArrayDeserializer**](https://github.com/apache/kafka/blob/trunk/clients/src/main/java/org/apache/kafka/common/serialization/ByteArrayDeserializer.java)), `ByteBuffer` ([**ByteBufferDeserializer**](https://github.com/apache/kafka/blob/trunk/clients/src/main/java/org/apache/kafka/common/serialization/ByteBufferDeserializer.java)), `Bytes` (um tipo de byte[] imutável do Kafka, [**BytesDeserializer**](https://github.com/apache/kafka/blob/trunk/clients/src/main/java/org/apache/kafka/common/serialization/BytesDeserializer.java)), `double` ([**DoubleDeserializer**](https://github.com/apache/kafka/blob/trunk/clients/src/main/java/org/apache/kafka/common/serialization/DoubleDeserializer.java)), `float` ([**FloatDeserializer**](https://github.com/apache/kafka/blob/trunk/clients/src/main/java/org/apache/kafka/common/serialization/FloatDeserializer.java)), `int` ([**IntegerDeserializer**](https://github.com/apache/kafka/blob/trunk/clients/src/main/java/org/apache/kafka/common/serialization/IntegerDeserializer.java)), `long` ([**LongDeserializer**](https://github.com/apache/kafka/blob/trunk/clients/src/main/java/org/apache/kafka/common/serialization/LongDeserializer.java)), `short` ([**ShortDeserializer**](https://github.com/apache/kafka/blob/trunk/clients/src/main/java/org/apache/kafka/common/serialization/ShortDeserializer.java)), `String` ([**StringDeserializer**](https://github.com/apache/kafka/blob/trunk/clients/src/main/java/org/apache/kafka/common/serialization/StringDeserializer.java)), `UUID` ([**UUIDDeserializer**](https://github.com/apache/kafka/blob/trunk/clients/src/main/java/org/apache/kafka/common/serialization/UUIDDeserializer.java)) e `null` ([**VoidDeserializer**](https://github.com/apache/kafka/blob/trunk/clients/src/main/java/org/apache/kafka/common/serialization/VoidDeserializer.java)).

As bibliotecas de deserialização vão depender de Schema, que falaremos depois. Pela [Confluent](https://docs.confluent.io/platform/current/schema-registry/fundamentals/serdes-develop/index.html#supported-formats) temos o [**KafkaAvroDeserializer**](https://github.com/confluentinc/schema-registry/blob/master/avro-serializer/src/main/java/io/confluent/kafka/serializers/KafkaAvroDeserializer.java) para [AVRO](https://avro.apache.org/docs/1.11.1/specification/), [**KafkaProtobufDeserializer**](https://github.com/confluentinc/schema-registry/blob/master/protobuf-serializer/src/main/java/io/confluent/kafka/serializers/protobuf/KafkaProtobufDeserializer.java) para [ProtoBuf](https://protobuf.dev/programming-guides/proto3/) e [**KafkaJsonSchemaDeserializer**](https://github.com/confluentinc/schema-registry/blob/master/json-schema-serializer/src/main/java/io/confluent/kafka/serializers/json/KafkaJsonSchemaDeserializer.java) para [JSON](https://www.json.org/json-en.html). Pela [Red Hat](https://www.apicur.io/registry/docs/apicurio-registry/2.4.x/getting-started/assembly-configuring-kafka-client-serdes.html), temos o [**AvroKafkaDeserializer**](https://github.com/Apicurio/apicurio-registry/blob/main/serdes/avro-serde/src/main/java/io/apicurio/registry/serde/avro/AvroKafkaDeserializer.java) para [AVRO](https://avro.apache.org/docs/1.11.1/specification/) e [**ProtobufKafkaDeserializer**](https://github.com/Apicurio/apicurio-registry/blob/main/serdes/protobuf-serde/src/main/java/io/apicurio/registry/serde/protobuf/ProtobufKafkaDeserializer.java) para [ProtoBuf](https://protobuf.dev/programming-guides/proto3/). Esses formatos dependem da troca de Schema entre o produtor e o consumidor, o que implica no uso de um servidor de registro de schemas, que são o [Schema Registry](https://docs.confluent.io/platform/current/schema-registry/index.html#about-sr) da Confluent e o [Apicurio Registry](https://www.apicur.io/registry/docs/apicurio-registry/2.4.x/index.html) da Red Hat.


Podemos ver abaixo o código da interface Deserializer. Devemos implementar o método `T deserialize(String topic, byte[] data);` e apesar de existir o um método com o argumento `Headers` ele não faz parte do corpo da mensagem e falaremos mais a frente.

```java
/**
 * An interface for converting bytes to objects.
 *
 * A class that implements this interface is expected to have a constructor with no parameters.
 * <p>
 * Implement {@link org.apache.kafka.common.ClusterResourceListener} to receive cluster metadata once it's available. Please see the class documentation for ClusterResourceListener for more information.
 *
 * @param <T> Type to be deserialized into.
 */
public interface Deserializer<T> extends Closeable {

    /**
     * Configure this class.
     * @param configs configs in key/value pairs
     * @param isKey whether is for key or value
     */
    default void configure(Map<String, ?> configs, boolean isKey) {
        // intentionally left blank
    }

    /**
     * Deserialize a record value from a byte array into a value or object.
     * @param topic topic associated with the data
     * @param data serialized bytes; may be null; implementations are recommended to handle null by returning a value or null rather than throwing an exception.
     * @return deserialized typed data; may be null
     */
    T deserialize(String topic, byte[] data);

    /**
     * Deserialize a record value from a byte array into a value or object.
     * @param topic topic associated with the data
     * @param headers headers associated with the record; may be empty.
     * @param data serialized bytes; may be null; implementations are recommended to handle null by returning a value or null rather than throwing an exception.
     * @return deserialized typed data; may be null
     */
    default T deserialize(String topic, Headers headers, byte[] data) {
        return deserialize(topic, data);
    }

    /**
     * Close this deserializer.
     * <p>
     * This method must be idempotent as it may be called multiple times.
     */
    @Override
    default void close() {
        // intentionally left blank
    }
}
```

Como todo deserializer deve seguir o mesmo padrão do Serializer, senão serão inconsistentes, vamos optar por uma solução simples usando [`ByteBuffer`](https://docs.oracle.com/javase/8/docs/api/java/nio/ByteBuffer.html) para deserializar nossos objetos de domínio. Segue abaixo como ficaria a implementação.

```java
public class WeatherInfoDeserializer implements Deserializer<WeatherInfo> {

    @Override
    public WeatherInfo deserialize(String topic, byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        double lat = buffer.getDouble();
        double lon = buffer.getDouble();
        double temperature = buffer.getDouble();
        double wind = buffer.getDouble();
        long timestamp = buffer.getLong();
        return new WeatherInfo(new Geolocation(lat, lon), temperature, wind, timestamp);
    }
}
```

```java
public class GeolocationDeserializer implements Deserializer<Geolocation> {

    @Override
    public Geolocation deserialize(String topic, byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        double lat = buffer.getDouble();
        double lon = buffer.getDouble();
        return new Geolocation(lat, lon);
    }
}
```

# 2.3.2. Definindo as configurações

O próximo passo é definir as configurações do Consumidor. Quando vamos inicializar qualquer cliente Kafka, ele pode receber como parâmetro um [**Properties**](https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html) ou um **Map<String, Object>** com os valores que serão usados para configurar o consumidor e tudo relacionado a ele (deserializers, interceptors, etc...). Os valores aceitos como padrão estão na [documentação do site do Kafka](https://kafka.apache.org/documentation/#consumerconfigs), é uma leitura obrigatória. 

![Documentação das configurações padrões do Producer com uma boneca e um homem palito](./imagens/consumer-configs.png)

Fonte: https://excalidraw.com/#json=P4ZmiPhqVFdCSC2Vo27XV,kdl5j-Z1aBDH_u1L9H7jOg

Abaixo eu listo as configurações mais importantes, mas você não precisa decorar essas chaves, pode usar a classe [**ConsumerConfig**](https://kafka.apache.org/34/javadoc/org/apache/kafka/clients/consumer/ConsumerConfig.html) que além de ter toda as chaves, tem toda a documentação associada a ela.

* `key.deserializer`
* `value.deserializer`
* `bootstrap.servers`
* `group.id`
* `group.instance.id`
* `partition.assignment.strategy`
* `isolation.level`
* `interceptor.classes`
* `heartbeat.interval.ms`
* `session.timeout.ms`

Assim para definir as propriedades que vamos usar basta:

```java
Properties properties = new Properties();
properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
properties.put(ConsumerConfig.GROUP_ID_CONFIG, "meu-primeiro-consumidor");
properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, GeolocationDeserializer.class);
properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, WeatherInfoDeserializer.class);
```

Diferentemente do Produtor, o consumidor exige que seja definido um ID do Grupo. Esse ID será usado para garantir que uma mensagem seja enviada apenas uma vez para um grupo. Consumidores com mesmo ID criam um cluster.

![Explicação de como funciona um cluster de consumidores](./imagens/consumer-cluster.png)

# 3.3.3. Instanciando e usando o Consumidor

Agora precisamos definir a classe que vai encapsular o consumidor. As responsabilidades dela serão:

* Escutar o Tópico
* Consumir automaticamente as informações 
* Agregar todas as informações pela chave fornecida

```java
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
}
```

Porque estamos recebendo [**Producer**](https://kafka.apache.org/34/javadoc/org/apache/kafka/clients/producer/Producer.html) como parâmetro? Para que possamos depois testar usando [**MockProducer**](https://kafka.apache.org/34/javadoc/org/apache/kafka/clients/producer/MockProducer.html)!

Recomendo você conhecer um pouco da classe [**ProducerRecord**](https://kafka.apache.org/34/javadoc/org/apache/kafka/clients/producer/ProducerRecord.html) e da própria classe [**Producer**](https://kafka.apache.org/34/javadoc/org/apache/kafka/clients/producer/Producer.html). Com a classe **ProducerRecord** podemos definir qual é a partição, o timestamp e os cabeçalhos da mensagem. Mas a partição pode ser definida [**Partitioner**](https://kafka.apache.org/34/javadoc/org/apache/kafka/clients/producer/Partitioner.html).

Mais um ponto a se observar é que o envio de mensagens é síncrono. Isso significa que o Kafka coloca as mensagens em um buffer e envia a posteriori, para mais detalhes veja as configurações `buffer.memory`, `batch.size` e `linger.ms` em [Producer Configs](https://kafka.apache.org/documentation/#producerconfigs).

Se você olhou a documentação, também deve ter percebido que Existe a possibilidade de se implementar transações, o que garante que várias mensagens sejam enviadas atomicamente.

[Voltar](./02-criando-um-produtor.md)