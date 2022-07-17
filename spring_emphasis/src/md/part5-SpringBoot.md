# 1-SpringBoot自动配置过程

主要还是@SpringBootConfiguration这个注解，它Import了一个ImportSelector（@SpringBootConfiguration→@EnableAutoConfiguration → AutoConfigurationImportSelector），这个Selector需要在最后Import SB，这样能方便我们扩展和覆盖，因为SpringBoot有Condition的特性，如果不在最后才被解析，那么Condition的条件就很难判断了，总不能提前让Selector把自动配置类都装进IOC容器，再一个一个地排除吧。 

AutoConfigurationImportSelector会以EnableAutoConfiguration的全类名为Key，从【所有依赖】的【类路径】下的【META-INF/spring.factories的文件】里找到【需要自动配置的类】的全类名，这是SpringBoot的初筛。然后再从初筛结果里过滤掉不满足Condition注解的类，最后剩下的就是本次SpringBoot工程需要引入的自动配置类的全类名。 

# 2-SpringBoot的jar包能直接运行的原因

首先需要明白一件事：java -jar本身是JVM的命令，JVM规范会从被运行的jar包里的一个目录下找到一个manifest文件，它其实是一个配置文件，然后找到这个配置文件的Main-Class配置项声明的全类名，然后运行这个类的main方法。

这样就有点思路了，也就是说，运行了这个类的main方法，就会运行SpringBoot工程，我一开始以为这个启动类就是SpringBoot的启动类，其实不是这样的。想要SpringBoot打包成可运行Jar包得在maven工程里声明一个插件，这个插件会在打包的时候自动注入一个JarLauncher，这个JarLauncher才是java -jar的启动类。这个启动类会先加载这个SpringBoot工程用到的依赖类，然后在开一个线程去执行SpringBoot启动类的main方法。那么怎么定位到SpringBoot启动类呢？毕竟JarLauncher它不会在打包的时候将SpringBoot启动类的全类名写死进去，其实它是在manifest文件，通过Start-Class的配置项去找到SpringBoot启动类的全类名的。

# 3-SpringBoot启动过程

执行SpringBoot启动类后，它到底做了什么？

首先是初始化阶段，它加载了spring.factories里key为【初始化器】和【监听器】的类，并将启动类作为配置类，初始化一个SpringApplication对象。

初始化结束后，通过初始化后的SpringApplication对象调用run方法进入启动阶段，在这一步发布一个SpringBoot启动的事件。

然后发布读取配置的事件，实现读取环境变量、配置信息。

然后创建IOC容器（AnnotationConfigServletWebServerApplicationContext），配置一些上下文，顺便发布一些事件。

接着再refresh IOC容器来进行容器初始化，**容器的初始化是基于SpringBoot的启动类作为配置类的，所以在初始化的时候会进行知识点1说的加载自动配置类**。

然后就是调用onRefresh()创建内置的tomcat容器，从而注册DispatchServlet。

可以说SpringBoot一个很大的魅力是【扩展】，就拿这个启动来说，我们可以自己实现初始化器和监听器，让SpringBoot工程在初始化阶段读取到它们，然后在启动阶段实现我们自定义的代码。

# 4-SpringBoot内置Tomcat的启动过程

我们先说一下最重要的原因：引入spring-boot-starter-web依赖后，某个jar的spring.factories引入了一个Servlet容器的自动配置类，这个自动配置类Import了好几个Servlet容器的配置类：

```java
@AutoConfiguration
@AutoConfigureOrder(-2147483648)
@ConditionalOnClass({ServletRequest.class})
@ConditionalOnWebApplication(
    type = Type.SERVLET
)
@EnableConfigurationProperties({ServerProperties.class})
@Import({ServletWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar.class, EmbeddedTomcat.class, EmbeddedJetty.class, EmbeddedUndertow.class})
public class ServletWebServerFactoryAutoConfiguration {
    public ServletWebServerFactoryAutoConfiguration() {
    }
}
```

这些配置类会通过Conditional注解判断是否注入容器内，主要是通过【是否有相关容器依赖】注入的，因为默认使用Tomcat，所以其他两个都不会命中Conditional注解，所以不会注入。具体的Servlet容器配置类会@Bean一个对应Servlet容器的创建工厂：

```java
@Configuration(
    proxyBeanMethods = false
)
@ConditionalOnClass({Servlet.class, Tomcat.class, UpgradeProtocol.class})
@ConditionalOnMissingBean(
    value = {ServletWebServerFactory.class},
    search = SearchStrategy.CURRENT
)
static class EmbeddedTomcat {
    EmbeddedTomcat() {
    }

    @Bean
    TomcatServletWebServerFactory tomcatServletWebServerFactory(ObjectProvider<TomcatConnectorCustomizer> connectorCustomizers, ObjectProvider<TomcatContextCustomizer> contextCustomizers, ObjectProvider<TomcatProtocolHandlerCustomizer<?>> protocolHandlerCustomizers) {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.getTomcatConnectorCustomizers().addAll((Collection)connectorCustomizers.orderedStream().collect(Collectors.toList()));
        factory.getTomcatContextCustomizers().addAll((Collection)contextCustomizers.orderedStream().collect(Collectors.toList()));
        factory.getTomcatProtocolHandlerCustomizers().addAll((Collection)protocolHandlerCustomizers.orderedStream().collect(Collectors.toList()));
        return factory;
    }
}
```

在SpringBoot工程启动的时候，在refresh（初始化） IOC容器时，不是会走自动配置类的解析嘛？就会解析到Servlet容器的自动配置类，接着根据Conditional选择注入Tomcat工厂。起码走完这一步是有工厂了，这个是毋容置疑的。那么在哪里调用工厂来创建Tomcat呢？IOC容器在初始化完成后会调用onRefresh方法，在SpringBoot的IOC容器里，这个onRefresh方法会通过class在容器里找到Servlet容器工厂，然后就能找到Tomcat工厂了，不过值得注意的是，在这一步它只允许容器存在一个Servlet容器工厂，不然就会启动时抛异常了，这个挺好理解的。最终通过Tomcat工厂去创建Tomcat，然后启动Tomcat，接着就是通过一个线程去挂起Tomcat等待后续的请求到来了。

# 5-SpringBoot读取配置文件过程

其实知识点3就说过了，是基于事件的方式进行的。SpringBoot启动的时候会发布一个【读取配置文件】的事件，当监听器被实例化后（**注意它作为SpringBoot基础对象，是由SpringBoot实例化的，可以通过自动配置的方式创建，但不是作为SB注入后才使用**），就会读取相关的配置文件。这里是通过一个叫配置文件监听器的Listener去监听的。

# 6-SpringBoot日志

默认的日志框架是logback，但统一使用的日志规范是slf4j，logback只是它的其中一个实现方式而已。想要使用日志的话，必须得满足3样东西

1. 日志规范，slf4j
2. 日志桥接器：不同日志实现有不同的桥接器，算是【日志实现】与【日志规范】的桥梁
3. 日志框架：具体的日志实现，需要用【日志桥接器】与【日志规范】进行桥接

比如log4j它需要log4j-to-slf4j这个桥接器的依赖，才能整合slf4j。只不过SpringBoot默认使用logback-classic + logback这套组合而已。

# 7-SpringBoot的一些其他扩展点

为什么SpringBoot的有些扩展点需要以spring.factories的方式配置呢？很简单，许多扩展点在IOC创建之前就要调用了，总不能通过注入IOC容器的方式吧？只能依赖SpringBoot的自动配置读取来创建自定义扩展点的对象。

https://blog.csdn.net/m0_48358308/article/details/122142658
