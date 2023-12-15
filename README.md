# Imersão Kafka

## Objetivo

O objetivo desse material é ser um curso sobre [Apache Kafka](https://kafka.apache.org/) gratuito e open source. Como o material é Open Source, ele pode ser livremente distribuído mantendo a informação de autoria, não deve ser comercializado. Esse material desenvolvido por [Victor Osório](github.com/vepo/) usando somente a documentação do Apache Kafka.

## Apresentações

* [Curso Imersão Kafka - FLISoL 2023](/recursos/imersao-kafka.pdf)
* [Modelagem de Sistemas Event Driven - FLISoL 2023](/recursos/modelagem-de-sistemas-event-driven.pdf)
  - [Vídeo](https://youtu.be/4AjkkjohVNk?si=in-g1OzoBoRu5nqE)

## Ferramentas necessárias

* OpenJDK 8 ou superior
* Maven
* Docker
* Descompactador tar (no Windows [7-Zip](https://7-zip.org/download.html))

Para instalar o Java e o Maven, recomendo usar o [SDKMan!](https://sdkman.io/install). Com essa ferramenta é fácil alterar a versão do Java mudar variáveis de ambiente e instalar várias ferramentas.

## Roteiro

1. [Como configurar um Broker Apache Kafka](./01-configurar-broker-kafka.md)
2. [Como criar um Produtor de Mensagens](./02-criando-um-produtor.md)
3. [Como criar um Consumidor de Mensagens](./03-criando-um-consumidor.md)
4. [Como criar um tópicos](./04-criando-um-topico.md)
5. [Como criar um Stream](./05-criando-um-stream.md)
6. [Como configurar um Cluster Kafka](./06-criando-um-cluster.md)
