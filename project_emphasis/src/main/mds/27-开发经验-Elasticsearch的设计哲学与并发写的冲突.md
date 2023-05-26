# 前提-Elasticsearch的设计哲学

并非高性能的强一致性的数据库，而是最终一致性、高可用、适合高并发读场景的搜索引擎。

# 问题背景与分析

对Elasticsearch的文档进行并发写入的时候，有可能会报出版本冲突异常，即version conflict。大致意思是对文档的写入时，文档的实际版本号与预期版本号不一致。Elasticsearch为了提升整体的吞吐量，采用乐观锁解决冲突。

为什么不使用悲观锁？悲观锁其实适用于读写竞争大的场景，这种竞争会导致读和写的性能产生冲突。回到Elasticsearch的设计哲学，它通过乐观锁牺牲了写的并发性，从而称为一个高并发读的搜索引擎。[官方手册](https://www.elastic.co/guide/en/elasticsearch/reference/current/optimistic-concurrency-control.html)是这样讲的：

**Optimistic concurrency control**

Elasticsearch is distributed. When documents are created, updated, or deleted, the new version of the document has to be replicated to other nodes in the cluster. Elasticsearch is also asynchronous and concurrent, meaning that these replication requests are sent in parallel, and may arrive at their destination out of sequence. Elasticsearch needs a way of ensuring that an older version of a document never overwrites a newer version.

To ensure an older version of a document doesn’t overwrite a newer version, every operation performed to a document is assigned a sequence number by the primary shard that coordinates that change. The sequence number is increased with each operation and thus newer operations are guaranteed to have a higher sequence number than older operations. Elasticsearch can then use the sequence number of operations to make sure a newer document version is never overridden by a change that has a smaller sequence number assigned to it.

可以提炼到5点信息：

1. 文档进行写操作的时候，会通过CAS的方式替换文档的版本号，并同步到其他节点的副本上。
2. 文档写操作进行同步的时候，采用异步 + 并发的方式，更新其他节点的文档信息。
3. 因为是异步多线程同步，文档的写操作的**同步有序性得不到保证**，可能一个旧的写请求 会比 新的写请求 晚到节点
4. 为了避免 旧写 覆盖 新写，Es给每个文档增加了version字段。在进行写操作同步的时候，Es会通过CAS操作对其他副本进行更新，如果副本上的文档version比同步请求的文档version不相等，说明发生了旧写 覆盖 新写的情况，或者**更新的写**发生提前写入。
5. 不管是哪种场景，只要发生version不相等，Es都会报出版本冲突一样，即上面的version conflict，同时回滚写入行为（最终一致性）。

当然，报异常只是Es默认的降级策略，我们也可以通过retry_on_conflict参数指定版本冲突后的重试次数，如果写并发量不大的话，通过重试次数也能解决这个问题。

# 结合业务的思考

目前（截止202212），项目中使用Elasticsearch存储用户行为日志，更多是应用在新增、读场景，没有对同一份文档进行并发写的场景。

虽然如此，如果真的要用到并发写，除了Es本身提供的重试次数外，有没有比较好的解决方案呢？首先分布式锁可以不可以？应用程序代码通过分布式锁，控制同一时刻只有1个进程进行Es写操作。

可以是可以，但回到Es的设计哲学，本身为了满足高并发读而采用乐观锁，应用程序还要自己加一把分布式的悲观锁来解决版本冲突，在使用层面上与Es的理念背道而驰。

但是实际业务场景没必要为了兼容Es的设计哲学而做出让步，如果真的有并发写场景，加分布式锁也未尝不可，**毕竟锁的只是进程，并不会影响其他进程对Es的高效读，我认为这套方案是可行的。**