# Apache Kafka do ZERO: Kafka Gerenciado com Strimzi

P: Existe uma forma fácil de fazer deploy do Kafka no Kubernetes?
R: SIM! E ainda usar tudo Open Source. Só usar o strimzi.io

## Um pouco de Kubernetes

Tutorial: https://github.com/vepo/k8s-tutorial

1. Para que serve Kubernetes?
    * Kubernetes é um mecanismo de orquestração de contêineres de código aberto para automatizar a implantação, o dimensionamento e o gerenciamento de aplicativos em contêineres. O projeto de código aberto é hospedado pela Cloud Native Computing Foundation (CNCF). 
      https://kubernetes.io/docs/home/
2. Qual é o grande conceito que devemos entender para usar Kubernetes?
    * Qualquer aplicação será composta por imagens e RECURSOS
3. O que é um Recurso?
    * É uma definição de algo que pode ser configurado no ambiente. 
        * Servidor
        * Broker
        * Cluster
        * Tabela de um banco de dados
        * Tópico
4. Quais são os Recursos padrão do Kubernetes?
    * **Namespaces**: Permitem a segmentação de um cluster em ambientes virtuais. Eles são úteis para cenários em que vários usuários ou equipes precisam de isolamento.
    * **POD**: 	PODs são a menor peça de um deploy que pode ser criado e gerenciado pelo Kubernetes.
    * **Service**:Um serviço é uma abstração, significa que uma aplicação ou um conjunto de PODs está exposto como um serviço de rede.
    * **Deployments**: Um Deployment provê uma forma declarativa de controlar PODs e ReplicaSets.
    * **ReplicaSet**: O objetivo de um ReplicaSet é manter um conjunto estável réplicas de pods em execução a qualquer momento. Como tal, costuma ser usado para garantir a disponibilidade de um número especificado de pods idênticos.
    * **StatefulSets**: Gerencia a implantação e o dimensionamento de um conjunto de pods e garante a ordem e unicidade dos pods. É usado para aplicativos com requisitos de estado, como bancos de dados.
    * **DaemonSet**: Garante a execução de um POD em todos (ou apenas alguns) nós.
    * **Jobs**/**CronJobs**: Jobs criam um ou mais pods e garantem que um número específico de pods termine com sucesso. CronJobs criam jobs em uma programação especificada, similar a tarefas cron em sistemas Unix.
    * **Persistent Volumes** (PVs) e **Persistent Volume Claims** (PVCs): PVs são uma abstração de armazenamento físico no cluster, enquanto PVCs são pedidos de armazenamento feitos pelos usuários. Juntos, eles facilitam o gerenciamento de armazenamento persistente.
    * **ConfigMaps** e **Secrets**: ConfigMaps são usados para armazenar dados de configuração em pares chave-valor, enquanto Secrets são usados para armazenar informações sensíveis, como senhas, tokens e chaves SSH.
    * **Ingress**: Um objeto API que gerencia o acesso externo aos serviços em um cluster, normalmente HTTP.
5. É possível extender o Kuberentes criandos novos Recursos?
    * SIM!!! É possível definir **CustomResourceDefinitions** e definir coisas apenas usando YAMLs. 
      https://kubernetes.io/docs/tasks/extend-kubernetes/custom-resources/custom-resource-definitions/

## O que seria o Strimzi?

Strimzi é uma forma de se manter clusters Kafka em Kubernetes os definindo como Recursos.

Ver: https://strimzi.io/

## Tutorial

### Pré requisitos
* WSL ou Linux
* Minikube
* kubectl

### Passos
1. Criar cluster usando mini
   ```bash
   minikube start --memory=4096
   ```
2. Criar namespace
   ```bash
   kubectl create namespace kafka
   kubectl config set-context --current --namespace=kafka
   ```
3. Criar CustomResourceDefinition
   ```bash
   kubectl create -f 'https://strimzi.io/install/latest?namespace=kafka' -n kafka
   ```
4. Criar Cluster
   ```bash
   kubectl apply -f ./recursos/strimzi/kafka-cluster.yaml
   ```

## Operators

Operator é um software que transforma a definição do recurso em software em execução.

```bash
$ kubectl get pods
NAME                                          READY   STATUS    RESTARTS   AGE
my-cluster-entity-operator-85ff4d95cc-rdhmz   0/2     Running   0          4s
my-cluster-kafka-broker-0                     1/1     Running   0          3m11s
my-cluster-kafka-broker-1                     1/1     Running   0          3m11s
my-cluster-kafka-controller-2                 1/1     Running   0          3m10s
strimzi-cluster-operator-6c89b8f9dc-mrmlp     1/1     Running   0          5m38s
```

Isso significa que na listagem acima existem 2 operadores. O cluster operator vai criar toda infraestrutura para criação de um cluster Kafka. O entity operator vai configurar o cluster Kafka criando Tópicos e Usuários.

```bash
$ kubectl get all
NAME                                              READY   STATUS    RESTARTS   AGE
pod/my-cluster-entity-operator-85ff4d95cc-rdhmz   2/2     Running   0          12m
pod/my-cluster-kafka-broker-0                     1/1     Running   0          15m
pod/my-cluster-kafka-broker-1                     1/1     Running   0          15m
pod/my-cluster-kafka-controller-2                 1/1     Running   0          15m
pod/strimzi-cluster-operator-6c89b8f9dc-mrmlp     1/1     Running   0          17m

NAME                                 TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)                                        AGE
service/my-cluster-kafka-bootstrap   ClusterIP   10.108.138.158   <none>        9091/TCP,9092/TCP,9093/TCP                     15m
service/my-cluster-kafka-brokers     ClusterIP   None             <none>        9090/TCP,9091/TCP,8443/TCP,9092/TCP,9093/TCP   15m

NAME                                         READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/my-cluster-entity-operator   1/1     1            1           12m
deployment.apps/strimzi-cluster-operator     1/1     1            1           17m

NAME                                                    DESIRED   CURRENT   READY   AGE
replicaset.apps/my-cluster-entity-operator-85ff4d95cc   1         1         1       12m
replicaset.apps/strimzi-cluster-operator-6c89b8f9dc     1         1         1       17m
```

## Criando Tópicos

De forma similar, podemos definir os tópicos


```bash
kubectl apply -f recursos/strimzi/kafka-topics.yaml
```

Podemos verificar tanto usando as ferramentas do kafka, mas para isso é preciso acessar um dos pods.

```bash
kubectl exec -it my-cluster-kafka-broker-1 -- ./bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
kubectl exec -it my-cluster-kafka-broker-1 -- ./bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic my-topic-daily
```

Quanto usando as APIs do Kuberentes.

```bash
kubectl get kafkatopics
kubectl describe kafkatopic my-topic-daily
```