# 需要留意的问题

1. AOP本类调用会失效，那么和事务传播会不会冲突？

   假设类A有两个@Transactional方法a()和b()，a里面调用b，b里面的事务是不会生效的。b事务如果发生异常，本质是回滚a事务，而不是b事务。

   两种方式：

   1. A类注入自己，通过注入的自己来调用b()。
   2. exposeProxy设置为true，然后通过AopContext.currentProxy()获取自己的代理对象，通过这个代理对象来调用b()。

   **写一个demo验证一下！！！！！！！！！！！**

# 29-AOP的通知顺序

5.2.7之前：

​	正常：环绕前 → 前置 → 方法 → 环绕后 → 后置 → 返回通知

​	异常：环绕前 → 前置 → 方法  → 环绕后 → 后置 → 异常

5.2.7以后：

​	正常：环绕前 → 前置 → 方法 → 返回通知→ 后置 → 环绕后

​	异常：环绕前 → 前置 → 方法 → 异常 → 后置 → 环绕后

简单来说就是，新版本的Spring将后置处理器延后了，并且严格按照环绕通知的顺序，确保环绕通知是最开始与最后。

# 30-Spring AOP和AspectJ的关系

直接下定义：AspectJ本身也是一个AOP框架，但Spring AOP使用了AspectJ的许多概念，所以直接依赖了它。但是两者的实现机制完全不一样，可以说是Spring AOP使用了AspectJ，但只是使用了一部分，比如切点解析和匹配的功能，以及通知类型。

两者的区别是，AOP使用了动态代理，分别是JDK动态代理和CGLIB。而AspectJ使用的是静态代理，它可以在编译时进行扩展点织入的，也可以在编译后对class文件进行织入，还可以在类加载期间进行织入（不过需要自定义类加载器），还可以通过javaagent进行织入，并且使用AspectJ还需要它的专属编译器。而AOP可是在JVM运行时产生代理对象，使用起来更加方便。

# 31-JDK动态代理和CGLIB的区别

选择哪个：被代理的类实现了接口用JDK，反之用CGLIB。

JDK生成代理Class是通过JVM内部的方式，而CGLIB使用的是ASM框架。

JDK的代理Class只是实现了接口，对接口的方法进行增强。而CGLIB的代理Class使用的是继承，对父类的所有方法进行增强。

JDK实际调用时，通过代理Class去调用【处理类（就是InvocationHandler）】进行增强，通过反射的方式调用原方法。而CGLIB是通过子重写父，并且子增强父的方式实现增强和原方法调用。

所以CGLIB对于目标方法的调用是调用父类方法，而JDK动态代理调用目标方法的时候用的是反射。

CGLIB可以通过FastClass，实现【调用自身方法会增强】的特性，但Spring AOP为了进行统一，去掉了这个功能，**也就是说本类中通过代理方法1调用代理方法2，代理方法2不会被增强**。

# 32-AOP的使用方式

1. 通过实现接口的方式，比较古早，但利于了解源码
2. 通过xml配置
3. 通过JavaConfig

# 33-AOP失效的场景

1. 知识点31说过了，代理方法a里面通过this的方式调用代理方法b的话，代理方法b是不会被增强的，解决方式有2种：
   1. A类注入自己，通过注入的自己来调用b()。
   2. exposeProxy设置为true，然后通过AopContext.currentProxy()获取自己的代理对象，通过这个代理对象来调用b()。
2. 代理方法a是private

# 34-AOP是在哪里进行创建的？

其实结合知识点18已经有答案了，有2个地方会创建AOP：

1. A在初始化阶段通过从三级缓存获取ObjectFactory，调用getBean，最终是通过BeanPostProcessor的后置处理进行创建，然后将代理对象交给IOC容器。
2. A在解决循环依赖B期间，B通过三级缓存找到A的ObjectFactory的getBean进行A的AOP提前创建，然后代理A放入二级缓存中。当然，最终在A的生命周期里，代理A还是会交给IOC容器管理的。

# 42-Mybatis和Spring的桥梁：RegistryPostProcessor、扫描器、FactoryBean，动态代理

回想一下，在项目中使用Mybatis的时候，都是直接注入一个Mapper接口，就能直接使用写好的sql逻辑了，这是为什么呢？实际上注入的是被Spring AOP后的动态代理对象。

但是要明确一件事，当一个SB注解使用在接口上，接口也没有实现类时，Spring默认是不会将这个接口注册为Bean定义的。那Mybatis是如何做到的？其实Mybatis框架会向Spring注册一个BeanDefinitionRegistryPostProcessor，这个BeanDefinitionRegistryPostProcessor通过@MapperScan Import的一个重写了的扫描器，它会扫描到项目里配置好的Mapper接口，这样子Mapper接口就能作为Bean定义注册进BeanDefinitionMap了。

可以Mapper接口本质是一个接口，即使称为Bean定义了，还是不能实例化啊。其实Mybatis在创建Mapper的Bean定义时，都指定了Bean定义的beanClass是MapperFactoryBean，它是Mybatis的一个FactoryBean，还记得知识点14就说过，实例化SB的时候发现是FactoryBean接口的派生的话，会调用它的getObject()方法，这里就是偷梁换柱、接口变对象的地方了，最后在这个动态代理对象会被注入IOC容器了，供我们使用。 

四个桥梁的作用：

1. RegistryPostProcessor：将接口注册为Bean定义。
2. 扫描器（由@MapperScan引入）：将哪些接口注册为Bean定义，并且更换Bean定义的beanClass为MapperFactoryBean。
3. FactoryBean：使用JDK动态代理，返回Mapper接口的实现类。
4. 动态代理：接口变实现类的方式。
