# 问题描述

有这么一个背景，需要对A表的记录R先进行阻塞读，阻塞读成功后再给R记录进行修改，大致代码如下：

```java
@Transactional
public void businessX(){
	A r = aMapper.selectByCondictionForUpdate();
	aMapper.businessUpdate(r,param);
}
```

结果是businessUpdate的操作等待锁阻塞超时，最终失败。

# 问题分析

要知道ShardingJDBC在5.2版本以后，同一个事务下发生在写操作之后的读操作，会默认路由到主库。相反，发生在写操作之前的读操作，默认还是走从库的。

也就是说，selectByCondictionForUpdate默认走的从库，在businessX事务提交之前仍占有**从库A表记录R的锁L**。

当执行businessUpdate后，由于开启了全同步复制，从机也要执行businessUpdate语句，也就是要拿到从库A表记录R的锁L，但此时锁L被selectByCondictionForUpdate的事务占有、不释放。最终导致businessUpdate等待锁超时。

# 解决方法

执行读操作之前，执行**HintManager.getInstance().setMasterRouteOnly();**保证读操作发生在主库：

```java
@Transactional
public void businessX(){
    HintManager.getInstance().setMasterRouteOnly();
	A r = aMapper.selectByCondictionForUpdate();
	aMapper.businessUpdate(r,param);
}
```

保证selectByCondictionForUpdate和businessUpdate在同一数据库同一事务下进行，减少不必要的锁冲突。