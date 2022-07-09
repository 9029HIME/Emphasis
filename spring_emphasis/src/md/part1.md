# 0-一些待解决的问题

1. 到底是谁创建BeanDefinition,然后放进去Map里？

2. Aware的作用

   简单来说，如果SB实现了XXXAware接口，那么这个SB在初始化阶段会调用XXXAware接口的setXXX(XXX xxx)方法，具体怎么做就看你的方法实现了，一般是对方法参数的xxx对象进行一些处理。
   
3. 三级缓存还需要完善内容https://www.cnblogs.com/sniffs/p/13295558.html

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

2. 调用invokeBeanFactoryPostProcessor()方法，通过BeanFactoryPostProcessor根据【特定规则】比如@Component、比如@Import，比如@Bean扫描元数据，将符合要求的Bean从【概念态】变为【定义态】，注册进BeanDefinitionMap。

3.  再次调用其他BeanFactoryPostProcessor处理。

   **从【定义态】到【纯净态】**

4. 调用finishBeanFactoryInitialization方法来使用这个BeanDefinition，此时BeanFactory会一个一个扫描BeanDefinition，**判断每个BeanDefinition是否有资格被创建**，因为有些Spring Bean是懒加载或者多例，是不希望在IOC容器初始化时加载的。

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
   3. 初始化回调方法(比如@PostConstruct)
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

**首先给实例化下一个定义：创建对象。也就是知识点8里的实例化。**

1. 使用构造函数+反射的方式（Spring默认使用）。

   【以下方式需要开发者手动控制】

2. 指定这个Spring Bean的FactoryMethod（必须静态方法），FactoryMethod需要返回这个Spring Bean。

3. @Bean方法的方式，和FactoryMethod优先类似，这个方法也需要也需要返回这个Spring Bean。

4. 实现FactoryBean接口的方式，此时通过beanName获取的对象不一定是自身，而是【自己实现的接口方法的返回】。比如A implement FactoryBean，实现方法的过程中返回了B对象，那么getBean("a")实际返回的是B对象。

# 15-Spring Bean的装配

加入Spring Bean没有装配，那么Spring程序在启动后Spring Bean只是独立地存在着，不同Spring Bean之间没有任何关系。装配就是建立Spring Bean之间的依赖关系。有2种装配：手动（配置文件）、自动（依赖注入）。

# 16-SB的生命周期回调方法有哪些？

SB在整个生命周期中，涉及2种回调方法：2. 初始化回调 3.销毁回调

不管是初始化回调还是销毁回调，都有3种实现方式：注解、实现接口、注解属性，如果同时实现多个，那么优先级依次从高到低排序。

对于初始化回调来说，分别是：

1. 在SB内声明@PostConstruct注解的方法。
2. SB实现InitializingBean接口，实现它的afterPropertiesSet方法。
3. 通过@Bean配置SB时，指定initMethod属性，属性值是这个SB内的某个方法名。

对于销毁回调来说，分别是：

1. 在SB内声明@PreDestory注解的方法。
2. SB实现DisposableBean接口，实现它的destroy方法。
3. 通过@Bean配置SB时，指定destoryMethod属性，属性值是这个SB内的某个方法名。

# 17-SB的生命周期

其实和知识点7IOC加载过程有点类似，这里重点在SB，从大的方向看，可以分为5大部分：

1. BeanDefininition包装
2. 实例化
   1. 就是单纯的【创建对象】，虽然是SB，但SB本质就是一个Bean，这里就是创建这个本质的Bean，此时还没有SB相关的属性，相当于SB的雏形，但比起BeanDefinition，更像一个【我们想要的对象】了。 
   2. 实例化也会有多种方式，具体可以看知识点14。
3. 属性赋值
   1. 就是解析自动装配，进行依赖注入，在这一步会解决循环依赖的问题。
4. 初始化
   1. 调用实现的Aware的回调
   2. 前置处理（BeanPostProcessor）
   3. 调用初始化回调（知识点16）
   4. 创建动态代理（如果需要的话）
   5. 后置处理（BeanPostProcessor）
5. 销毁
   1. IOC容器关闭的时候，进行销毁回调（知识点16）

# 18-Spring如何解决循环依赖

循环依赖就跟递归一样，如果没有出口就会不停地循环下去，那么解决循环依赖的出口是什么呢？是Spring的3级缓存机制：

一级缓存：

​	存放【成熟态】的SB。

二级缓存：

​	暂时存放成熟态的SB，为了避免A↔B → C ↔ A导致【A被重复调用三级缓存的lambda表达式】，从而创建了多次动态代理。

三级缓存：

​	存放lambda表达式，参数是SB的实例和beanName。调用这个lambda表达式会创建SB的动态代理并返回，如果没实现动态代理的话只会返回这个SB的实例。最终将这个【SB动态代理】或【SB实例】放入【二级缓存】中。 

​	不立即调用这个lambda表达式是想尽量遵循生命周期规律，在初始化阶段创建动态代理，如果在实例化后就调用三级缓存，那么所有（包括不循环依赖）的SB在实例化后，就会立即创建动态代理。但循环依赖的话如果不提前创建，写入二级缓存，就会没有出口，可以说是不得已的设计。

具体是怎么使用呢？以SB-A和SB-B循环依赖实例，假设此时先创建SB-A：

1. 实例化SB-A之前，依次去【一级缓存】【二级缓存】【三级缓存】看有没有SB-A，如果存在，就没必要走实例化以及之后的操作了。假设现在不存在，就会走下面的流程：
2. 实例化SB-A，此时还是一个【纯净态】的SB，然后将SB对应的lambda表达式放到【三级缓存】。
3. 回顾知识点17，SB在实例化后，就会进行依赖注入，此时发现SB-A有个属性注入了SB-B，就会去创建SB-B。
4. 和SB-A一样，Spring会尝试实例化SB-B，并且在实例化之前依次去【一级缓存】【二级缓存】【三级缓存】看有没有现成的，假设现在没有，就会继续走下面的流程：
5. 实例化SB-B，此时还是一个【纯净态】的SB，然后将SB对应的lambda表达式放到【三级缓存】。
6.  SB-B实例化后，进行依赖注入，发现SB-B有个属性注入了SB-A，就会去创建SB-A。
7. 实例化SB-A之前，还是会依次查看【一级缓存二级缓存三级缓存】，此时SB-A只存在【三级缓存】里，一级缓存没有，二级缓存也没有，到了三级发现有了，于是就执行SB-A的lambda表达式。如果SB-A实现了动态代理就创建动态代理并返回，如果没有实现，则直接返回这个普通的SB-A。**然后将SB-A放到【二级缓存】中**。
8. 接着继续走SB-B的生命周期流程，如果此时SB-B和SB-C还有循环依赖，也是走同样的逻辑，只不过此时SB-B充当了SB-A的角色罢了。最终SB-B会被加到【一级缓存】里
9. 最终回到SB-A属性赋值那一步，继续进行SB-A的初始化。因**为初始化阶段涉及动态代理的创建，所以在这一步会找【二级缓存】，看看SB-A会不会在解决循环依赖期间提前创建动态代理了，如果有就不重复创建了。**
10. 最终将SB-A放入【一级缓存】，并且清除掉SB-A在【二三级缓存】的存在。

## 精炼

**如果只是单纯为了解决循环依赖的问题，其实用一个缓存就足够了。但所有对象都基于这个一级缓存的话，并发状态下有可能会获取到不完整的Bean，如果对【一级缓存】上互斥锁的话，又会影响性能。所以又加了一个【二级缓存】，但是如果只用二级缓存的话，多重循环依赖又会导致重复创建动态代理的情况，所以才设计了三级缓存。**

**具体是怎么使用呢？我先假设没有循环依赖，现在只需要getSIngleton去创建一个SB-A。在实例化之前先去【一级缓存】找有没有现成的，避免重复创建对象，如果没有就进行实例化步骤（参考知识点17），将早期的A对	象包装成ObjectFactory放进三级缓存里。然后进行属性注入（populateBean），接着就是初始化流程了比如各种Aware各种前置处理各种初始化回调，然后到初始化-创建AOP这一步，就会getSingleton从三级缓存拿到A的ObjectFactory，调用getBean方法获取A的动态代理对象，当然如果A没有使用动态代理，返回的只是A本身。然后进行后置处理等等，最终加入到一级缓存中**。

**正常情况下A的创建是用不到二级缓存的，说句实话Spring是不希望循环依赖的，否则今年（2022）5月份的时候SpringBoot也不会暴露一个“禁止循环依赖”的配置，按照正常的SB生命周期来说，是在初始化这一步才进行动态代理的创建。为了解决循环依赖的问题，在给A进行populateBean的时候，会创建SB-B，前面的流程和创建A一样，但是给B进行populateBean的时候，发现依赖了A，此时A是处于singletonsCurrentlyInCreation的状态，这是Spring维护的。于是在getSingleton的时候会查询到三个缓存，发现第三层缓存有A的ObjectFactory，于是就调用它的getBean()方法，提前创建了A的动态代理对象（当然如果A没有使用动态代理，返回的只是A本身），接着清掉三级缓存中A的存在，将A的动态代理对象放入二级缓存。这一步可以说是违背了SB的生命周期，将A的动态代理创建提前了。然后B的A属性赋值成功，继续B的生命周期，最终返回到A的populateBean。**

**接着继续A的生命周期，刚才说到因为循环依赖已经打破了A的生命周期规律，那么在初始化的时候还会再创建一次动态代理嘛？答案是不会的，因为A创建动态代理的时候，会通过getSingleton在三层缓存中依次找对象，正常来说要到第三层才找到，如果发现在第二层已经找到了，说明A已经经历过这一步了，这时候直接返回，做下一步处理，最终将A加入【一级缓存】。**



## 二级缓存能否解决循环依赖？

如果只是为了解决循环依赖没有出口的问题，那么一级缓存也足够了，但是无法避免出现【并发情况下获取不完整Bean的问题】。如果只使用二级缓存，就无法避免多重循环依赖导致重复创建动态代理。

## 是否有解决多例SB的循环依赖？

没有，因为多例Bean不会使用三级缓存的任意一个。

## 是否有解决SB构造函数的循环依赖？

没有，不过可以通过@Lazy解决。

# 19-BeanDefinition的加载过程

其实就是将【概念态】的SB转换为【定义态】，然后注入进BeanDefinitionMap的过程。我就说以JavaConfig方式吧，因为它是目前最主流的方式了。

首先是初始化IOC容器，也就是AnnotationConfigApplicationContext，在这个过程中通过BeanDefinitionReader读取配置，再通过【解析器】解析对应的Spring注解，比如@Bean，@Import，@Component等等。当解析到@ ComponentScan时，会通过Scanner去扫描这个路径下的所有Class文件，再判断是否标注了Spring注解，在这过过程中会排除接口和抽象类，最终将合格的SB创建为BeanDefinition。

# 20-如何对BeanDefinition注册完后做扩展

在实例化IOC容器时，会调用invokeBeanFactoryPostProcessor方法创建BeanDefinition，然后注册进BeanDefinitionMap里，注册这一步是通过BeanDefinitionRegistryPostProcessor进行的，所有BeanDefinition注册完会调用BeanFactoryPostProcessor修改BeanDefinition（值得注意的是BeanDefinitionRegistryPostProcessor是继承BeanFactoryPostProcessor的）。

也就是说只要自定义一个BeanFactoryPostProcessor的实现类，就能达到修改Bean定义的效果。 

# 21-如何对SB注册完后做扩展

和对BeanDefinition做扩展差不多，也是实现某个接口并注入IOC容器即可。在实例化IOC容器时，会调用finishBeanFactoryInitialization()方法循环Bean定义，然后创建SB。也就是说这个循环结束后，SB就会被完整创建好了。接着会进入下一个循环，这次循环是从一级缓存中找SB，看看这个SB是否实现了SmartInitializingSIngleton接口，如果是，就调用这个接口的afterSingletonsInstantiated()方法，也就是这个方法来完成SB的扩展处理的。

除了这个，在finishBeanFactoryInitialization()之后还会调用finishRefresh()，里面会发布ContextRefreshedEvent事件。也就是说，只需创建一个监听器去监听这个事件，并且注入IOC容器内，也能达到SB扩展处理的效果。



# 22-SB的生产顺序是如何确定的

SB的生产顺序直接由Bean定义的注册顺序确定的，Bean定义的注册顺序如下：

1. @Configuration最先
2. @Conponent
3. @Import一个Class
4. @Bean
5. @Import一个ImportBeanDefinitionRegister实现类
6. 扩展BeanDefinitionRegistryPostProcessor来手动注册
