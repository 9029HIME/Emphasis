# ELK

![image](https://user-images.githubusercontent.com/48977889/195508218-609a78d7-719c-4563-851d-25559ab6b8a3.png)

最经典的日志架构：通过LogStash完成日志收集，通过Elasticsearch完成日志检索，通过Kibana完成UI展示，这三个都是Elastic公司的产品，集成度非常高，可以通过简单的配置来完成三个项目的集成使用。

优点是部署简单、开箱机用，缺点是Logstash本身作为日志的收集工具，是比较耗CPU和内存资源的，需要有单独的机器来承载Logstash，否则很容易挤压应用程序的硬件资源。

# ELK+TCP推送

![image](https://user-images.githubusercontent.com/48977889/195508447-6af6ca8d-fce0-45b0-b574-6a92104f89cd.png)

既然Logstash收集和解析日志的过程很耗资源，那么可以换一种思路：日志收集从Logstash主动解析 转变为 由应用系统主动推动。通过logback的一个插件，可以将日志通过网络传输的方式，直接发给Logstash进行收集。

这样的优点非常明显：减少了Logstash的硬件消耗。但也是有缺点的：每个系统都要更改一遍logback的配置，变相地增加了代码侵入性。

# FileBeat

![image](https://user-images.githubusercontent.com/48977889/195508582-55405b7b-726b-460e-82af-ba53c2b8116b.png)

![image](https://user-images.githubusercontent.com/48977889/195508521-287964c3-6d50-4e2a-9e4c-72eca905b1ae.png)

FileBeat也是Elastic公司的产品，和ELK架构不同的是，FileBeat是通过**监听日志文件**的方式获取日志内容，并不是Logstash主动收集、或者应用程序主动推送。对于FileBeat来说，它只关心文件的内容是否发生了变化，并将对应的变化告知对应的监听者。

FileBeat支持多种监听者，如Es、Logstash、Kafka、Redis。使用FileBeat的好处是：将日志的收集与应用系统隔离开，并且可以将日志发送给多种类型的监听者。但是FileBeat也有一个重大缺点：同一个FileBeat，只能将日志发送给一种监听者，无法同时广播给多种监听者，要么都是Kafka、或者要么都是Es，不能既发送给Kafka、又发送给Es。

# KEFK

有没有办法既兼顾FileBeat的优点，又能弥补其缺点呢？或许我们可以设置FileBeat将日志发送给MQ，通过MQ的多消费者特性，间接达成多监听者的效果，这就是KEFK：将FileBeat直接交互于Kafka，依赖Kafka高吞吐量的消息处理能力，将日志转发给LogStash，或者其他独立的Es集群：

![image](https://user-images.githubusercontent.com/48977889/195508663-54b7c2d8-c8ed-44ad-a43e-4a388f991a3b.png)

**当然缺点也是有的：太贵了，Kafka也是挺吃硬件资源的，因此这种架构还是适合大型互联网公司。其实对于中小公司来说ELK或者ELK+TCP推送已经能够满足要求了，这里只做架构认知层面的介绍。**