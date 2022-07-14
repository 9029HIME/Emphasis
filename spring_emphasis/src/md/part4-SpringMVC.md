# 1-DispatchServlet的工作流程

1. 请求到达SpringMVC工程后，工程会先判断DispatchServlet是否被初始化了，如果没有则初始化（第一次惩罚），如果有则路由到对应的handler。
2. 初始化DispatchServlet后，根据请求路径请求HandlerMapping映射器，拿到这个请求的执行链，执行链是包含了拦截器的处理流程（如果有的话）。这里有可能会报404。
3. 通过执行链调用HandlerAdapter这个适配器，执行这个请求对应的逻辑。
4. handler返回ModelAndView，工程通过视图解析器，解析最终的结果响应给调用者。

#  2-SpringMVC如何处理ajax请求

其实就是如何处理前后端分离之间的调用，用到了一个核心的处理类：HttpMessageConverter，请求和响应的解析就是通过这个类来处理的。SpringBoot默认使用Jackson来处理消息转换。转换这一步实际在【适配器执行handler】之前和【执行完handler之后】。

![image](https://user-images.githubusercontent.com/48977889/178902915-d1860d88-9603-431d-a821-c0730e53d348.png)

# 3-SpringMVC为什么需要父子容器

Spring和Spring MVC的SB其实是用两个IOC容器来存储的，SpringBoot只有1个，也就是说1个容器也是可以的。但为什么Spring MVC和Spring整合的时候要2个呢？其实主要是为了【划分框架界限】，秉承单一设计模式的原则，Handler和Mapper是交给Spring容器来管理，而Controller则是交给SpringMVC容器管理。Spring容器作为父容器是访问不到SpringMVC容器里的Controller，反之却可以，使整体架构规范。并且SpringMVC作为一个子容器能够更好的抽离出来，换成其他的（如Struts）。

总的来说，从功能上看1个容器是足够的，划分出父子容器只是为了规范架构和可扩展而已。当然现在主流都是使用SpringBoot，工程内的所有SB都是由1个容器去管理的，所以不太需要考虑父子容器的问题。

# 4-在父子容器的前提下，如果将MVC的SB交给Spring容器来管理，可行嘛？

不可以的，按道理来说子容器可以访问父容器的SB，可是在初始化DispatchServlet（其实是初始化HandlerMapping映射器里的HandlerMethods）的时候，SpringMVC是在【子容器】拿到所有的Bean定义名称，再通过Bean定义名称一个一个去【子容器】找Controller，然后初始化映射器的。如果将MVC的SB交给父容器来管理，初始化映射器的时候就会得不到结果，之后的请求的路由结果就会报404了。

# 5-在父子容器的前提下，如果将Spring的SB交给SpringMVC来管理，可行嘛？

结合知识点4，是可行的。毕竟SpringMVC优先从子容器找SB，找不到才去父容器，现在所有SB都在子容器里，是没问题的。不过不推荐这么做，有可能会导致事务或AOP失效。

# 不过现在主流使用SpringBoot，根本不用考虑父子容器的问题了，知识点3-5就当是扩展点来了解一下。

# 6-SpringMVC零配置的原理

早期配置SpringMVC需要各种配置，现在基于JavaConfig的就能实现了，原因是Spring3后提供了SPI机制，只需将【实现了特定接口】或【继承了特定类】的配置类放到【特定目录】下，SpringMVC工程在启动的时候就会加载这些配置类，达到类似配置文件的效果。

# 7-MVC的拦截器和Servlet的过滤器

拦截器是MVC独有的，它不依赖Servlet。拦截器只针对HandlerMapping映射成功后的请求，这个请求是能找到具体执行handler的，而过滤器会处理所有经过Servlet容器的请求。拦截器作为MVC的一部分，它能够访问IOC容器里的SB，可以执行DI的操作，而过滤器作为更底层的存在，是不支持的。

对于一个请求，先打到Servlet容器，在打到具体Servlet之前会经过过滤器，打到具体的Servlet准备执行具体逻辑时，才会进入拦截器，然后再到handler。

Filter → Servlet → Inteceptor → handler 