# 0-一些待解决的问题

1. 到底是谁创建BeanDefinition,然后放进去Map里？

# 1-Spring的优缺点是什么

## 优点

IOC、AOP、声明事务、单元测试、集成其他框架的能力，能够很大程度的简化开发，使我们拥有开箱即用的组件模块。

IOC提供了集中管理对象的功能，使对象与对象之间的耦合度降低，我们也能更好地维护对象了。

AOP可以让我们在不修改原有代码的情况下，扩展代码的额外功能，或者进行统一的处理，比如日志、前置后置处理等。

声明事务提供了更便捷的事务入口，可以让我们以注解的方式直接开发，不用关心事务的底层逻辑，但灵活性比较低。

## 缺点

计算机的软件架构都有一个通识：上层操作越简单，底层实现越复杂。Spring提供了非常强大的应用层功能，为我们开发提供巨大便利的同时，也屏蔽了一些底层细节，如果想去深入了解是比较困难的，需要花较多的时间。

# 2-IoC容器是什么

## 定义

IoC容器就是用来实现了IoC功能的容器，什么是IoC？IoC就是控制反转，说人话就是将【开发人员对组件对象的控制权】交给了一个【容器】，在程序的生命周期里都是由这个【容器】来控制对象的生命周期，实现依赖注入（DI）。对于Spring来说，这个IoC容器就是【Spring IOC】。

## 优点

在实际开发中，用最少的代码实现了IoC的功能，只需在类上声明Spring实例注解如@Component、@Controller等，就可以在程序启动时将对象注入IoC容器内。需要使用的时候只需@Autowired进行依赖注入即可，不用频繁地new。**同时支持懒加载模式**。

## 实现机制

之前看过一部分源码，直接下一个定义：Spring的IoC机制是基于【工厂模式】+【反射】实现的，其中【工厂】的直接体现是【BeanFactory】，【反射】体现在

实际创建Spring Bean的时候。

## IoC和DI的区别？

IoC是一种【控制反转】的思想与规范，就跟JVM规范一样，是虚的，Spring对IoC的实现是【Spring IOC容器】+ DI的方式。其中DI指的是依赖注入，开发者可以通过DI可以在Spring IOC容器里获取对象。从本质上来讲，**DI是实现IoC的一个环节**。

# 3-BeanFactory

BeanFactory是整个Spring的基石，【是Spring IOC容器的本质】，基本上所有Spring Bean都是交给它创建、存储的。它是Spring的【顶层接口】，实现了【简单工厂的设计模式】，最核心的方法是getBean(...)，通过传入标识来生产一个Spring Bean：

![ApplicationContext与BeanFactory的关系](https://user-images.githubusercontent.com/48977889/177089887-3245ce7f-a8c7-42ce-af83-ff15e3aaab9e.png)

因为是顶层接口，所以实际干活还是依赖它的实现。它有非常多实现类，每个实现类有着【不同且单一】的职责，最强大的工厂是：DefaultListableBeanFactory，Spring底层就是使用这个工厂创建Spring Bean的：

![image](https://user-images.githubusercontent.com/48977889/177089363-e6c2ec3b-8a17-4b7d-9082-5f40bd0137ac.png)

# 4-BeanDefinition的作用

BeanDefinition是用来【存储Spring Bean的元数据】的，比如Bean是否单例、是否懒加载、是否Primary、是否抽象、自动装配方式、属性值等等。比起Class，BeanDefinition存储了更多关于【Spring Bean生命周期】的元数据，可以看做是Class的装饰增强版，**BeanFactory也是基于BeanDefinition的信息来生成Spring Bean的**。基于知识点3的图做一个优化：

![image](https://user-images.githubusercontent.com/48977889/177170047-354c4ffd-e633-4db6-9a4a-70d23c2a6a90.png)

Bean先被Spring包装成BeanDefinition装进BeanDefinitionMap里，此时key是beanName，value是对应的BeanDefinition。BeanFactory再循环遍历BeanDefinitionMap，根据BeanDefinition定义的元数据创建每个Spring Bean。

# 5-Spring Bean的4种形态（提前预热了解）：

![image](https://user-images.githubusercontent.com/48977889/177170090-1a8b09af-0547-476e-afd5-49297a8a809c.png)

# 6-BeanFactory和ApplicationContext有什么区别？

他俩都被叫做Spring IOC容器，只不过BeanFactory层级上属于ApplicationContext的下层。在早期版本中大家经常使用ClassPathXmlApplicationContext，那时候还得通过xml配置文件来配置每个Spring Bean的元数据，后面基本上使用注解的方式配置Bean，才会使用AnnotationConfigApplicationContext。

但是在继承关系上，ApplicationContext实现了BeanFactory，但是ApplicationContext却包含了BeanFactory，那到底是谁在生产Spring Bean呢？看了下源码可以发现，ApplicationContext的getBean本质是调用了BeanFactory的getBean，这个BeanFactory是属于ApplicationContext的。也就是说，**本质是BeanFactory在生产Spring Bean**：

```java
public Object getBean(String name) throws BeansException {
    this.assertBeanFactoryActive();
    return this.getBeanFactory().getBean(name);
}
```

比起BeanFactory只是纯粹地生成Spring Bean，ApplicationContext做了更多的事，比如Bean到BeanDefinition的转换，加载环境变量，事件监听等等。换个更好理解的说法：ApplicationContext像4S店，而BeanFactory更像是汽车工厂。当我想要1个汽车（Spring Bean）时，我会选择去4S店购买（从ApplicationContext获取），而不是去汽车工厂购买（BeanFactory），但是汽车工厂也能作为汽车容器（Spring IOC容器）使用。**所以我们一般称ApplicationContext为Spring IOC容器，而BeanFactory是Spring IOC容器的本质**。

# 7-Spring IOC容器的加载过程

之前看过源码，在捋一遍。首先知识点6已经说明了ApplicationContext是IOC容器，那么IOC容器是在什么时候加载的呢？或者说在哪个环节加载的呢？在Spring的设计里，**它是在创建1个ApplicationContext对象时就加载IOC容器了，也就是在new AnnotationConfigApplicationContext()的时候。**

结合知识点5：

1. 将Spring Bean从【概念态】转变为【定义态】，既根据元数据创建1个Spring Bean的BeanDefinition对象。
2. 将BeanDefinition放入BeanDefinitionMap里，等待BeanFactory进行Spring Bean的创建。
3. BeanFactory循环BeanDefinitionMap，将里面的BeanDefinition通过反射创建【纯净态】的Spring Bean，此时的Spring Bean里面还没有【依赖注入】的对象值，Spring也是通过这个设计来解决【依赖循环】的问题。 
4. BeanFactory为【纯净态】的Spring Bean赋值【依赖注入】属性，最终生成可以使用的Spring Bean。

细节：

​		**从【概念态】到【定义态】**

1. 创建ApplicationContext对象，会走以下流程。

2. 通过BeanFactoryPostProcessor根据【特定规则】比如@Component、比如@Import，比如@Bean扫描元数据，将符合要求的Bean从【概念态】变为【定义态】，注册进BeanDefinitionMap。

3.  再次调用其他BeanFactoryPostProcessor处理。

   **从【定义态】到【纯净态】**

4. 调用finishBeanFactoryInitialization方法来实例化这个BeanDefinition，此时BeanFactory会一个一个扫描BeanDefinition，**判断每个BeanDefinition是否有资格被创建**，因为有些Spring Bean是懒加载或者多例，是不希望在IOC容器初始化时加载的。

5. BeanFactory先去singletonObjects看看这个Spring Bean是否已经被创建了（通过beanName），如果有则直接返回，不重复创建。

6. BeanFactory通过反射创建这个Spring Bean，此时还处于【纯净态】，里面还没有依赖注入的属性。

   **从【纯净态】到【成熟态】**

7. 判断是否需要依赖注入，如果要，进行依赖注入。

8. 判断是否需要调用这个Spring Bean的Aware接口，如果要，则调用。

9. 判断是否需要回调，如果要，则调用。

10. 将这个Spring Bean放入singletonObjects。

# 8-Spring扩展接口

Spring扩展递减指的是Spring在创建Spring Bean的时候，Bean【概念态】到【成熟态】会调用的方法，这些方法可以是Spring自己声明好的，也可以是我们通过实现接口的方式声明的。总的来说，可以分为5个大类：

1. BeanDefinitionRegistryPostProcessor：在知识点7加载IOC容器时，用来【注册并加载BeanDefinition】到Map里。

2. BeanFactoryPostProcessor：在知识点7加载IOC容器时，修改Map里的BeanDefinition，也就是修改Bean的元数据。但是在继承关系上，BeanDefinitionRegistryPostProcessor实现它。 

3. 然后在Bean从【定义态】到【成熟态】时，会调用9种BeanPostProcessor，注意是9种。BeanPostProcessor的执行时机是这样的：
   1. 实例化Bean（此时只是一个普通对象）
   2. 调用BeanPostProcessor前置处理
   3. 调用初始化方法
   4. 调用BeanPostProcessor后置处理

4. 在Bean的初始化阶段，会调用XXXAware接口的setAware方法。
5. 在Bean的初始化阶段，会调用初始化扩展接口，比如InitializationBean，@PostConstruct等。

# 9-配置Spring Bean的方式

其实就是定义Spring Bean概念态的方式，包括

1. xml配置（早期会这样用）。
2. 注解，如@Component、@Service、@Repository，但是得基于component-scan配置，SpringBoot自动集成了component-scan功能。
3. @Bean，与@Component不同的是，@Bean创建的对象是人为创建的，而@Component是基于工厂+反射创建的。
4. @Import，可以直接引入第三方的对象。

# 10-Spring Bean的作用域

有2种方式可以配置Spring Bean的作用域：1. xml属性 2. @Scope注解。一共有4种作用域：

1. 单例：1个IoC容器1个bean
2. 多例：每次获取都是不同的bean
3. 单个请求：1个请求1个bean
4. 单个session：1个Session1个bean
5. application：1个ServletContext1个bean

# 11-为什么Spring Bean默认是单例的？

归根结底，就是为了性能。首先不可否认的是，我们交给Spring管理的bean基本上是【辅助完成业务功能】的bean，属于项目级别的，不会落实到具体业务，总不能将handler内使用的业务对象交给Spring管理吧？因此1个对象就能满足开发要求。

使用单例模式来生成这些bean，【减少了多余的分配内存操作】，【也减少了垃圾对象的产生】，并且单例的话只需通过beanName从singletonObjects这个缓存里获取即可，也【提高了bean的获取速度】。

1. 单例就能满足项目要求。
2. 减少内存分配和动态代理带来的性能消耗。
3. 减少垃圾对象。
4. 获取速度快。

# 12-Spring Bean的线程安全性

一般开发过程中不要对Spring Bean的成员变量进行【写】操作，毕竟它默认是单例的，所以并发操作下Spring Bean不是线程安全的。对于局部变量倒是线程安全的，毕竟它存在局部变量表里，每一个线程都是独有的。

# 13-Spring Bean如何处理并发操作问题？

关于知识点12，如果实在要写成员变量，有以下几种解决方式：

1. 将Bean声明为多例，但一般很少这样做。
2. 使用ThreadLocal，但无法操作公共的成员变量，此时成员变量也是线程独有的。
3. 加入同步机制，如sychronized或lock。

# 14-实例化Spring Bean的方式

首先给实例化下一个定义：创建对象。也就是知识点8里的实例化。

1. 使用构造函数+反射的方式（Spring默认使用）。

   【以下方式需要开发者手动控制】

2. 指定这个Spring Bean的FactoryMethod（必须静态方法），FactoryMethod需要返回这个Spring Bean。

3. @Bean方法的方式，和FactoryMethod优先类似，这个方法也需要也需要返回这个Spring Bean。

4. 实现FactoryBean接口的方式，此时通过beanName获取的对象不一定是自身，而是【自己实现的接口方法的返回】。比如A implement FactoryBean，实现方法的过程中返回了B对象，那么getBean("a")实际返回的是B对象。
