# 2. Como criar um Produtor de Mensagens

[Voltar](./01-configurar-broker-kafka.md)


# 2.1. Criando um projeto Java e configurando as bibliotecas necessárias

Para criar o projeto Java, vamos usar o Maven para gerenciar dependências e automatizar a build. O projeto já está criado ([ver](./produtor/meu-primeiro-produtor)).

Caso você não tenha experiência em Maven, é um dos gerenciadores de build mais famosos do Java. Recomendo a leitura de uma introdução que escrevi para projetos Quarkus [Maven 101](https://github.com/dev-roadmap/backend-roadmap/blob/main/caso-de-uso-00-configurando-um-projeto-quarkus.md#maven-101) ou de uma série completa de posts do Chandra Guntur [Understanding Apache Maven – The Series](https://cguntur.me/2020/05/20/understanding-apache-maven-the-series/) (em inglês).

Para gerar um projeto Maven, use o comando abaixo.

```bash
mvn archetype:generate -DgroupId=io.vepo.kafka.imersao -DartifactId=meu-primeiro-produtor -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false
```

Depois de criado o projeto, abra o arquivo _Project Object Model_ `pom.xml` e adicione a biblioteca na área de dependências. As dependências Maven são representadas pelas coordenadas (`groupId`, `artifactId` e `version`) e podem ser encontrada nos repositórios [sonartype](https://central.sonatype.com/artifact/org.apache.kafka/kafka-clients/3.4.0/overview) ou [mvnrepository](https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients/3.4.0).

```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>3.4.0</version>
</dependency>
```

> **_Sobre o arquétipo:_** O arquétipo do projeto Java é bem antigo e desatualizado. Pode remover a dependência do JUnit 4 e mudar a versão do Java de `1.7` para `1.8`.
>
> **_Sobre a versão do Java:_**: As bibliotecas do Apache Kafka são compatíveis com as novas versões do Java, mas usaremos a 8. Se quiser pode testar depois como fica nas versões 11 (LTS), 17 (LTS) e 20 (mais recente).

# 2.2. Criando a classe produtora e informando o Maven que ela deve ser executada

O Maven segue a filosofia **convenção-sobre-configuração**, ou seja, para criar uma classe não precisamos informar em lugar nenhum que ela deve ser compilada, deve-se apenas colocar ela no diretório correto.

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
|   |   |                   └── WeatherSensorCollector.java
|   |   └── resources        ## Arquivos que não serão compilados, mas estarão disponíveis em tempo de execução
│   └── test                 ## Código usado para testes unitários
|       ├── java             ## Código Java para testes unitários
|       └── resources        ## Arquivos que não serão compilados, mas estarão disponíveis em tempo de execução
└── pom.xml                  ## Arquivo que define como será feita a build
```

Criada a classe, podemos verificar se está tudo certo executando ela usando o Maven:

```bash
mvn clean compile exec:java -Dexec.mainClass=io.vepo.sensor.weather.WeatherSensorCollector
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
                <mainClass>io.vepo.sensor.weather.WeatherSensorCollector</mainClass>
            </manifest>
        </archive>
    </configuration>
</plugin>
```

Para executar é preciso primeiro criar o jar:

```bash
mvn clean package
java -jar target/meu-primeiro-produtor-1.0-SNAPSHOT.jar
```

# 2.3. Implementando o Produtor

Antes de implementar essa classe, vamos criar alguns pressupostos sobre como ela vai ser utilizada.

1. Ela será utilizada em um loop de coleta dedados. Ela deve ser passada inicializada para o loop.
2. Ela receberá um objeto com informações do clima.
3. A chave será a geolocalização e esse valor está dentro das informações do clima.
4. A classe já receberá o produtor inicializado.

```java
WeatherSensorCollector collector = new WeatherSensorCollector(new KafkaProducer(/* inicialização */));
collector.send(new WeatherInfo(/* inicialização */));
```


[Voltar](./01-configurar-broker-kafka.md)