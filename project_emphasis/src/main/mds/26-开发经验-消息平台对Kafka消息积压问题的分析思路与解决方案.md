# 背景

在202209，因为B端项目组的业务特性，在B端项目组接入消息平台后，某些时间段的消息数量飙升，频繁触发Sentinel降级逻辑，导致上游系统可用性下降。为了优化这个问题，需要将消息平台的接入方式由**外部接口**过渡为**MQ**，在中间件选型上，决定复用决策引擎系统的的Kafka。

**在正式使用Kafka处理消息之前，为了应对未来可能在生产环境发生的消息积压问题，需要提前评估好分析思路与解决方案。**

其实个问题和消息可靠性一样，可以围绕以下3点：

1. 生产者生产太快。
1. Borker处理不及时。
2. 消费者消费太慢。

# 从消费者角度分析

一般来说生产者的生产速度，是很难控制的，所以优先去考虑消费者消费慢的问题。主要可以围绕以下几点：

1. **代码方面**：消费逻辑的代码有没有问题？
   1. 比如内存泄露导致GC频繁拖慢了消费速度？毕竟有些内存泄露在高并发的场景下才会暴露。
   1. 比如消费过程中因为连接池资源耗尽，导致处理消费速度变慢？
   1. 比如消费过程中下游接口请求异常、超时，导致业务流程阻塞？
   1. 比如代码写的不合理，调用了性能较差的API？
   1. 比如SQL写的不合理，执行了慢SQL？

1. **运维方面**：能否临时扩容消费者节点，增加某个Topic的消费速度？
   1. 这个方案得看场景。如果partition数量 ＞ 原有消费者数量，扩容是有效的。如果partitoin数量 ≤ 原有消费者数量，是无意义的。**这一点在[Kafka基础](https://github.com/9029HIME/Kafka_Learn/blob/master/src/mds/05%20Kafka%E6%B6%88%E8%B4%B9%E8%80%85.md)已经讲过：一个partition的leader，只会被一个消费者组的一个消费者所消费。**
   1. 消息平台使用的是333（3 Broker、3 Partition、3 Replica）架构，但消息平台的实例数只有2个，因此临时扩容消费者节点还是有效的。


# 从Broker角度分析

如果消费者角度优化后，问题还在，按优先级来看，该轮到Broker了。主要围绕运维层面，能否临时扩容Parition数量？

1. 这一点和扩容消费者节点一样，需要看场景。如果partition数量 ＜ 原有生产者数量，扩容是有效的，反之则无效。
2. 目前生产者主要来源C端与B端不同系统，而消息平台的Topic采用333架构，partition数量是 ＜ 原有生产者的，因此临时扩容partition还是有效的。**但是！！！partition的扩容有风险，会影响消息在不同replica的位置，比较考验运维人员的能力，需慎重评估**。
3. 扩容还需要考量硬件性能与成本的问题。

# 从生产者角度分析

如果从消费者、Broker角度优化后，还是有消息积压的情况，最后需要考虑生产者了。主要围绕以下几点：

1. **设计方面**：能否开启降级策略，减少消息发送量？
   1. 比如通过一个支持热更新的配置项（Apollo or Nacos），在发现消息积压严重的时候，将这个配置项设为true。
   2. 此时代码不会走发MQ的逻辑，而是走降级逻辑，将消息进行入库处理。
   3. 等积压的消息被消费者消费完后，再将配置项改为false，此时代码会走发送MQ逻辑。
   4. 同时备份好一个定时任务，将2.入库的降级消息进行重新消费。
2. 这个方案在评估的时候有些争议
   1. 因为消息平台本身属于公司的基础设施，没有任何业务属性。
   2. 生产者来自C端与B端的不同系统，在它们眼里，只是把消息托付给消息平台，由消息平台发送给对应的接收者。
   3. **如果还要搭配降级策略使用，上游系统的接入成本就变高了，需要系统各自处理好降级、兜底、重发的逻辑**。
   4. 最终，这个方案通过消息平台提供starter-api进行接入，starter-api包含了降级策略，只需配置好数据库表、降级配置、重发任务即可。**但是这个功能的测试资源不是很多，所以仅作为兜底策略，一般不太建议使用**。

# 但是

关于消息挤压问题，我请教了几位不同公司的朋友，
