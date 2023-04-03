# 2. Como criar um Produtor de Mensagens

[Voltar](./01-configurar-broker-kafka.md)


# 2.1. Criando um projeto Java e configurando as bibliotecas necessárias

Para criar o projeto Java, vamos usar o Maven para gerenciar dependências e automatizar a build. O projeto já está criado  ([ver](./produtor/meu-primeiro-produtor))

```bash
mvn archetype:generate -DgroupId=io.vepo.kafka.imersao -DartifactId=meu-primeiro-produtor -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false
```

[Voltar](./01-configurar-broker-kafka.md)