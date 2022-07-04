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

实际创建Sring Bean的时候。

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
