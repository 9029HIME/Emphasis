# 背景

比方说有这么一段业务代码：

```java
public void businessA(){
    Message message = outSideUtil.getOutsideMsg();
    handleMsg(message);
    outSideUtil.pushMsg(message);
}
```

大致逻辑是：

1. 通过outSideUtil请求外部系统，获得数据。
2. 在 本系统 对数据进行处理。
3. 将处理完成的数据 推送到 下游系统。

# 需求

现在我要对businessA()的业务逻辑 进行 代码埋点处理，比方说 检查handleMsg(message)前后的数据差异、处理耗时。

# 实现

有一个不错的方案，基于监听者模式 + SPI方式实现，首先将代码改造为：

```java
public void businessA(){
    Message message = outSideUtil.getOutsideMsg();
    
    DataSupport.preHandle(message);
    handleMsg(message);
    DataSupport.postHandle(message);
    
    outSideUtil.pushMsg(message);
}
```

DataSupport本质是一个监听器的触发器，将message投递到 注册在 触发器的监听器列表里，一个一个的监听器 进行处理：

```java
public class DataSupport{
    private Logger logger = LoggerFactory.getLogger(DataSupport.class);
    private List<DataListener> listeners = new ArrayList<>();
    
    public static final preHandle(Message message){
        for(DataListener listener : listeners){
            listener.preHandle(message);
        }
    }
    
    public static final postHandle(Message message){
        for(DataListener listener : listeners){
            listener.postHandle(message);
        }
    }
}
```

至于监听器是怎么来的？则是采用SPI机制的方式，将外部依赖的监听器（也就是DataListener的实现类）注入进去：

```java
public class DataSupport{
    private static Logger logger = LoggerFactory.getLogger(DataSupport.class);
    private static List<DataListener> listeners = new ArrayList<>();
    
    static{
        ServiceLoader<DataListener> loader = ServiceLoader.load(DataListener.class);
        Iterator<DataListener> iterator = loader.iterator();
        while(iterator.hasNext()){
            listeners.add(iterator.next());
        }
    }
    
    public static final preHandle(Message message){
        for(DataListener listener : listeners){
            listener.preHandle(message);
        }
    }
    
    public static final postHandle(Message message){
        for(DataListener listener : listeners){
            listener.postHandle(message);
        }
    }
}
```

这样，只要外部依赖 以 DataListener全类名 为文件名，以 实现类全类名 为内容，创建SPI文件，并将SPI文件放到外部依赖resources下META-INF.services文件夹就好了。之后核心项目只需引入外部依赖，就能实现代码埋点的拓展。

# 思考

感觉这个实现，完全可以将 SPI 替换成 项目内的接口实现类，甚至对关键代码进行AOP增强，也是可以的。

目前余额计算是通过定义接口实现类的方式，注入进IOC容器，在项目初始化的时候 从IOC容器内拿到 所有实现类，再注入进监听者集合里。日志记录则是AOP增强，获取处理前 和 处理后的信息。但这两个方案，都是在项目内的扩展实现。

而SPI是项目外的扩展实现，可以完全不动项目里的代码，通过外部引入依赖从而实现增强。这两个方法没有谁最好，只能说看具体应用场景吧。