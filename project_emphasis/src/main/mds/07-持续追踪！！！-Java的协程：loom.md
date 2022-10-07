# 前提

目前JDK19的Loom只不过是预览版特性，可能在将来的LTS会发生改动

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    IntStream.range(0, 10_000).forEach(i -> {
        executor.submit(() -> {
            Thread.sleep(Duration.ofSeconds(1));
            return i;
        });
    });
}
```



# 目前已知

1. 和Golang的Goroutine类似，也是Java自己实现了一个调度器，负责调度loom与lwt之间的关系，应该也是类似Golang的GMP模型那一套
2. loom的栈信息会在被切换的时候，暂时保存到堆空间里，等loom被成功调度后，又会从堆空间里找回属于自己的栈信息使用。也就是说，如果loom开得比较多，有可能会引发栈空间溢出。

