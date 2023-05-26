# 错误描述

伪代码：

```java
@Transactional
pubic void business(){
    businessHandle1();
    businessHandle2();
    businessHandle3();
}
```

结果：出现了businessHandle1和businessHandle2的结果，却没有businessHandle3的结果，在生产环境出现了业务不完整的脏数据。

# 原因

还记得早期参加工作的时候，IDEA有一个Alibaba的代码规范插件，里面有一个规定是：@Transactional必须指定rollbackFor异常，当时作为新人的我是不理解的，@Transactional不是发生异常后默认回滚吗？除了某些特定场景，需要XX异常才回滚，其他不都是最直接回滚就好了吗？为什么还要指定一遍rollbackFor = Exception.class？

那是因为，**@Transactional默认只会回滚RunnableException及其派生异常**。我们在业务系统中自定义的异常都继承自RunnableException，所以不指定rollbackFor也没有问题。上面的Bug发生原因是：businessHandle3()里面有小概率情况下会触发ParseException，而ParseException是直接继承自Exception，和RunnableException没有父子继承关系。因此当ParseException被抛出后，如果@Transactional没有指定rolllbackFor = Exception.class，就会回滚失败，直接提交事务。

# 总结

还是得养成@Transational默认异常的习惯啊，虽然Bug不是我引起的，但如果是由我来做这个功能，说不定也会犯这个错误。