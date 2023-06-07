# Cluster

## 磁盘与内存节点

理解Cluster之前，首先要理解什么是磁盘节点和内存节点。要知道普通的Cluster是没有主从之分的，它更像一个分片集群。集群上的节点都能创建交换机与队列，也能够存储消息。区别是**磁盘节点会将交换机、队列、绑定关系、消息进行持久化存储，而内存节点只会存在内存。**默认情况下启动一个RabbitMQ节点就是磁盘节点，除非显示指定它是内存节点：

```
rabbitmq-server start -ram
```

## 队列与队列引用

还是那句话：普通的Cluster是没有主从之分的，它们都是独立的节点，共同组成了一个集群，假设我通过3个磁盘节点nodeA、NodeB、NodeC创建一个RabbitMQ Cluster：

![01](05-Cluster与Mirror%20Cluster.assets/01.png)

我可以分别在不同节点上创建交换器、队列、绑定关系，在哪创建就属于哪个节点，假设在NodeA创建Q1，NodeB创建Q2，NodeC创建Q3：

![02](05-Cluster与Mirror%20Cluster.assets/02.png)

Q1实际创建在NodeA里，Q2实际创建在NodeB里，Q3实际创建在NodeC里，队列在非创建节点上以引用的方式存在，这个引用最终会指向队列的创建节点里。打个比方，假如Consumer通过NodeC消费Q1的消息，NodeC通过Q1引用从NodeA拉取消息，再转发给Consumer，消息实际还是从NodeA消费，包括Consumer的ack也是NodeC转发给NodeA，**NodeC本身不存储Q1、Q2的消息，只是有一种“从NodeC消费的假象”，生产者同理**。

## SpringAMQP与Cluster

在SpringBoot连接RabbitMQ集群的时候，通常会采用`,`将节点信息分隔开，比如：

```yaml
spring:
  rabbitmq:
    addresses: nodeA:5672,nodeB:5672,nodeC:5672
    username: username
    password: password
    virtual-host: /
```

如果我在代码上通过@Bean创建交换机、队列、绑定关系，最终会在哪个节点上创建？实际上，Spring Boot 配置中，指定了三个节点（nodeA、nodeB、nodeC）的地址。当应用启动时，它会尝试依次连接到 addresses 列表中的每个 RabbitMQ 节点，直到成功连接到一个节点为止。**也就是说默认为连接nodeA，最终代码上的交换机、队列、绑定关系都创建在nodeA上。**

那有没有办法可以向`队列和队列引用`那样分开创建呢？其实可以通过自定义ConnectionFactory和RabbitAdmin的方式指定创建位置：

```java
@Configuration
public class RabbitConfiguration {

    @Bean
    public ConnectionFactory connectionFactoryA() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("nodeA");
        connectionFactory.setUsername("username");
        connectionFactory.setPassword("password");
        return connectionFactory;
    }

    @Bean
    public ConnectionFactory connectionFactoryB() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("nodeB");
        connectionFactory.setUsername("username");
        connectionFactory.setPassword("password");
        return connectionFactory;
    }

    @Bean
    public ConnectionFactory connectionFactoryC() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("nodeC");
        connectionFactory.setUsername("username");
        connectionFactory.setPassword("password");
        return connectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdminA(ConnectionFactory connectionFactoryA) {
        return new RabbitAdmin(connectionFactoryA);
    }

    @Bean
    public RabbitAdmin rabbitAdminB(ConnectionFactory connectionFactoryB) {
        return new RabbitAdmin(connectionFactoryB);
    }

    @Bean
    public RabbitAdmin rabbitAdminC(ConnectionFactory connectionFactoryC) {
        return new RabbitAdmin(connectionFactoryC);
    }

    @Bean
    public DirectExchange exchange1() {
        return new DirectExchange("E1");
    }

    @Bean
    public Queue queue1() {
        return new Queue("Q1");
    }

    @Bean
    public Binding binding1(DirectExchange exchange1, Queue queue1) {
        return BindingBuilder.bind(queue1).to(exchange1).with("RK1");
    }

    @PostConstruct
    public void setupQueuesAndExchanges() {
        rabbitAdminA.declareExchange(exchange1());
        rabbitAdminA.declareQueue(queue1());
        rabbitAdminA.declareBinding(binding1());

        // 同样的，通过rabbitAdminB和C创建交换机、队列、绑定关系 
    }
}
```
通过上面的代码，可以实现`队列与队列引用`描述的队列分布。

# Mirror Cluster

关于镜像队列的介绍，以及Cluster和Mirror Cluster的区别和为什么要用Mirror Cluster，之前已整理过[笔记](https://github.com/9029HIME/Emphasis/blob/master/rabbitmq_advance/md/03-%E9%95%9C%E5%83%8F%E9%98%9F%E5%88%97.md)，这里不再赘述。在这里主要补充和强调3点：

## 队列的Master与Slave

Master和Slave是针对queue来说的，和`SpringAMQP与Cluster`不一样的是，Mirror Cluster作为一个拥有数据备份的集群，即使在NodeA创建了queue，NodeB也不会单纯地创建queue的引用，而是会创建这个queue的副本。**所以Mirror Cluster重点不是将queue创建到哪个节点，而是指定queue在哪个节点上作为Master：**

```java
Map<String, Object> args1 = new HashMap<>();
args.put("x-queue-master-locator", "nodeA");
channel.queueDeclare("Q1", true, false, false, args1);

Map<String, Object> args2 = new HashMap<>();
args.put("x-queue-master-locator", "nodeB");
channel.queueDeclare("Q2", true, false, false, args2);

Map<String, Object> args3 = new HashMap<>();
args.put("x-queue-master-locator", "nodeC");
channel.queueDeclare("Q3", true, false, false, args3);
```

如上面的代码所示，Q1在NodeA上作为Master存在，Q2在NodeB上作为Master存在，Q3在NodeC上作为Master存在，如图：

![03](05-Cluster与Mirror%20Cluster.assets/03.png)

## Producer与Consumer与主从

不管Producer和Consumer连接的是这个队列的Master还是Slave，发送和消费消息都是基于Master进行的。如果Producer把消息发送到NodeB的Q1，该消息将会被 NodeB 转发到 NodeA的Q1。然后 NodeA 会存储消息，并且将消息复制到其它的Q1的Slave 节点（包括 NodeB）。所以在最终状态下，所有节点（包括 master 和 slaves）都将存储该消息的副本。

同样的，消费者连接到 NodeB 并消费队列 Q1 的消息时，NodeB 实际上会从 NodeA（作为队列 Q1 的 master）拉取消息，然后将这些消息传递给消费者。虽然在 NodeB 上也有队列 Q1 的副本，但是消费者从 NodeB 获取的实际上是 NodeA 上的消息。

## SpringAMQP与故障转移

```
spring:
  rabbitmq:
    addresses: nodeA:5672,nodeB:5672,nodeC:5672
    username: username
    password: password
    virtual-host: /
```

Spring AMQP 的 CachingConnectionFactory 会管理与 RabbitMQ 集群的连接，并且它对于故障转移是有感知的。当当前连接的节点不可用时，CachingConnectionFactory 会尝试连接到下一个节点。这个过程对于 RabbitTemplate 是透明的，RabbitTemplate 会继续使用与当前连接的节点进行通信。**但是对于SpringAMQP来说，它不知道这个连接成功的节点是Master还是Slave，毕竟Master和Slave是用来描述队列的，不过根据`Producer与Consumer与主从`可以发现，即使连接的是Slave，问题也不大，顶多是增加了一些网络开销**。