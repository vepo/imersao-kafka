# Kafka do Zero

## O que é o Apache Kafka?

Apache Kafka é uma plafatorma de Streaming de eventos distribuídos.

Ela é composta por:

1. Broker orientado a mensagens
2. Bibliotecas para consumo e produção de mensagens
3. Uma engine Data Stream Processing
4. Um servidor para conectar com outras fontes de dados

## O MEME dos casos de uso

> https://twitter.com/vepo/status/1790109635829014942

Porque essa imagem está errada?

1. Simplifica a forma de se implementar Kafka em produção
    * Não é simplesmente colocando o Kafka que você vai ter 
        * Data Streaming
        * Log Aggregation
        * Web Activity Tracker
        * Data Replication
    * Kafka não implementa Message Queueing
        > https://cwiki.apache.org/confluence/display/KAFKA/KIP-932%3A+Queues+for+Kafka

## Conceito de Queue e Publish/Subscribe

1. Queue
    * Também conhecido como comunicação ponto a ponto
    * Distribuição de carga
    * 1 mensagem consumida 1 vez
    * Não durável
2. Publish-Subscriber
    * Não tem Queue, mas Tópico
    * Criação de um canal de comunicação com DIVERSOS SISTEMAS
    * 1 mensagem consumida diversas vezes
    * Durável, mas pode não ser

Primeira definição pelo JMS 1.0! 
> https://jakarta.ee/specifications/messaging/3.0/jakarta-messaging-spec-3.0#two-messaging-styles

## Quais as vantagens do Apache Kafka

1. Alta vazão (_throughput_)
2. Alta escalabilidade (fator de escalabilidade 1)
    > https://ieeexplore.ieee.org/document/9507502

### Desvantagens

1. Alto processamento no cliente
    > Para IoT, use MQTT!

Usado principalmente como Message-Oriented Middleware

## Conceitos arquiteturais

1. Delegação de responsabilidades
    1. Serialização
    2. Configuração
2. Particionamento
3. Replicação
4. Distributed log
    > https://blog.vepo.dev/posts/anatomia-de-um-topico
5. Transacional
6. Clusters distribuídos
    * Broker
    * Consumer
    * Connect

## Garantias de Ordenação

* Sem ordenação
* Ordenação parcial (por partição)
* Ordenação Global

## Usos

1. Como Message Oriented Middleware
    1. Criando Producer
        > produtor\meu-primeiro-produtor\src\main\java\io\vepo\sensor\weather\WeatherSensorCollector.java
    2. Criando Consumer
        > consumidor\meu-primeiro-consumidor\src\main\java\io\vepo\sensor\weather\WeatherSensorConsumer.java
2. Como Data Stream Processing
    1. Criando Data Stream Processing
        > stream\meu-primeiro-stream\src\main\java\io\vepo\sensor\weather\WeatherSensorProcessor.java
3. Conectando Bases externas
    1. O que é Kafka Connect
    2. O que é Kafka Connector (Sink e Source)