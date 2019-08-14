# Sentinel-Demo

一种提供分布式限流的解决方案。

* 限流只是一个最基本的服务治理/服务质量体系要求
  * 流量的切换
  * 能不能够正对不同的渠道设置不同的限流策略
  * 流量的监控
  * 熔断
  * 动态限流
  * 集群限流
  * ....
* 承接过双十一、实时监控。

具体使用。

https://github.com/alibaba/Sentinel/wiki/%E4%BB%8B%E7%BB%8D

引入jar包

```xml
 <dependency>
            <groupId>com.alibaba.csp</groupId>
            <artifactId>sentinel-core</artifactId>
            <version>1.6.3</version>
        </dependency>
```

首先构建规则，也就是限流的规则，例如之前的单机guava限制了每秒生成10个令牌。对于sentinel也是一样，我们需要初始化规则。

* 初始化限流规则
* 根据限流规则进行限流

```java
public class SentinelDemo {

    private static void initFlowRules(){
        List<FlowRule> rules = new ArrayList<>();
        FlowRule flowRule = new FlowRule();
        flowRule.setResource("doTest");//资源，被保护的，一般是方法或者接口；
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);//限流的阈值类型，这里有两种，一种是qps，一种是线程数
        flowRule.setCount(10);//表示数量
        rules.add(flowRule);
        FlowRuleManager.loadRules(rules);
    }

    public static void main(String[] args) {//限流的实施
        initFlowRules();
        while(true){
            Entry entry = null;//资源的实体
            try {
                entry = SphU.entry("doTest");
                System.out.println("Hello Pop");
            }catch (BlockException e){//如果被限流，将会抛出这个异常
                e.printStackTrace();
            }finally {
                if(entry!=null){
                    entry.exit();//释放
                }
            }

        }
    }

}
```

我们可以启动控制台去查看，前往官网的有realease的内容。

![1565623645375](./img/1565623645375.png)

启动参数

```
java -jar -Dserver.port=8888 -Dcsp.sentinel.dashboard.server=localhost:8888 -Dproject.name=sentinel-dashboard xxx.jar
```

由于这是一个监控的平台，所以我们将这个刚刚创建好的服务加入到dashborad监控中去

![1565623963521](./img/1565623963521.png)

并且监控还需要引入额外的jar包

```xml
<dependency>
            <groupId>com.alibaba.csp</groupId>
            <artifactId>sentinel-transport-simple-http</artifactId>
            <version>1.6.3</version>
        </dependency>
```

### Sentinel 限流的思考

* 限流用了什么算法来实现（滑动窗口）
* 它是怎么实现的（责任链有关系）
* SPI的扩展



### 基于注解方式实现

切面需要引入这个依赖

```xml
<dependency>
            <groupId>com.alibaba.csp</groupId>
            <artifactId>sentinel-annotation-aspectj</artifactId>
            <version>1.6.3</version>
        </dependency>
```

```java
@RestController
public class SentinelController {

    /**
     * 针对方法级别的限流
     * @return
     */
    @SentinelResource(value = "syayHello")
    public String sayHello(){
        System.out.println("hello world");
        return "hello world";
    }

}
```

但是由于需要切面拦截，所以我们还需要写一个切面的bean，提供给spring

```java
@Configuration
public class ApoConfiguration {

    @Bean
    public SentinelResourceAspect sentinelResourceAspect(){
        return new SentinelResourceAspect();
    }

}
```

制定规则。

```java
public class DemoApplication {

    public static void main(String[] args) {
        initFlowRules();
        SpringApplication.run(DemoApplication.class, args);
    }

    //初始化规则
    private static void initFlowRules(){
        List<FlowRule> rules = new ArrayList<>();
        FlowRule flowRule = new FlowRule();
        flowRule.setResource("doTest");//资源，被保护的，一般是方法或者接口；
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);//限流的阈值类型，这里有两种，一种是qps，一种是线程数
        flowRule.setCount(10);//表示数量
        rules.add(flowRule);
        FlowRuleManager.loadRules(rules);
    }
}

```

### Sentinel源码的解读

Sentinel的限流算法基于滑动窗口，但是其实本质实现的手法并不负责，他将时间切割成一个又一个可以看做是窗口的区域，并通过开始时间和结束时间来计算当前的窗口索引，并是否落到了当前的窗口上，如果落到了，就返回当前窗口，如果发现计算出来的时间大于了当前窗口表示，当前窗口已经过期，需要将窗口后移。

同时，Sentinel将所有被定义成Resource的资源，纳入自己的管辖范围之内，然后将所有的资源拼凑成树节点，这很好理解，文件中的资源系统就是一个一个的树节点。

当请求过来的时候，会通过他们组成链路的一个一个考验，这个链路的前半段是一些容错的检测，中间的轮盘表示

![1565709769709](./img/1565709769709.png)

他的算法的具体验，也就是滑动窗口的具体体验，每一个滑动窗口都会记录自己开始时间和结束时间，并且会记录这个滑动窗口进入请求的次数。

然后就是FlowSlot，我们之前定义的规则，我们要规定这个请求是否符合我们制定的规则，如果不符合，我们会用我们的方式将这个请求如何处理。后面则是一连串的请求方式

```java
public class DefaultSlotChainBuilder implements SlotChainBuilder {

    @Override
    public ProcessorSlotChain build() {
        ProcessorSlotChain chain = new DefaultProcessorSlotChain();
        chain.addLast(new NodeSelectorSlot());
        chain.addLast(new ClusterBuilderSlot());
        chain.addLast(new LogSlot());
        chain.addLast(new StatisticSlot());
        chain.addLast(new SystemSlot());
        chain.addLast(new AuthoritySlot());
        chain.addLast(new FlowSlot());
        chain.addLast(new DegradeSlot());

        return chain;
    }

}
```

### Sentinel 整合 Dubbo

首先将dubbo的项目调整好，然后再整合Sentinel之前，需要引入一个jar包

```xml
<dependency>
            <groupId>com.alibaba.csp</groupId>
            <artifactId>sentinel-dubbo-adapter</artifactId>
            <version>1.6.3</version>
        </dependency>
```

设置限流规则

```java
@SpringBootApplication
public class SentinelProviderApplication {

    public static void main(String[] args) throws IOException {
        initFlowRule();//设置限流规则
        SpringApplication.run(SentinelProviderApplication.class, args);
        System.in.read();
    }

    private static void initFlowRule(){

        FlowRule flowRule = new FlowRule();
        // 有参数的情况下，也需要有参数 sayHello(java.lang.String)
       ////请注意，这里拦截的是接口 
        flowRule.setResource("com.pop.sentinel.SentinelService:sayHello()");
        flowRule.setCount(10);
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        FlowRuleManager.loadRules(Collections.singletonList(flowRule));
    }

}
```

然后用jmeter进行压测

![1565795637772](./img/1565795637772.png)

发现请求是成功限流了。

### 更加丰富的限流方式

如果你不希望某个来源的请求，例如不希望某个模块例如我们创建的，sentinel-consumer的请求，可以这样设置。

```java
private static void initFlowRule(){

        FlowRule flowRule = new FlowRule();
        // 有参数的情况下，也需要有参数 sayHello(java.lang.String)
        //请注意，这里拦截的是接口
        flowRule.setResource("com.pop.sentinel.SentinelService:sayHello()");
        flowRule.setCount(5);
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);

        //限定来源的请求
        flowRule.setLimitApp("sentinel-consumer");

        FlowRuleManager.loadRules(Collections.singletonList(flowRule));
    }
```

但是你看到这里，可能以为这也许会读取dubbo配置中的应用名信息，但其实不是。

![1565796381539](./img/1565796381539.png)

之前讲解dubbo源码的时候，谈到了有一个隐式参数的问题，`setAttachment`，请注意，key不能写错。当然我们可以用不带隐式参数的内容看一看。做一个对比。

```java
@GetMapping("sayHello")
    public String sayHello(){
        RpcContext.getContext().setAttachment("dubboApplication","sentinel-consumer");
        return sentinelService.sayHello();
    }

    @GetMapping("sayHello2")
    public String sayHello2(){
//        RpcContext.getContext().setAttachment("dubboApplication","sentinel-consumer");
        return sentinelService.sayHello();
    }
```

![1565796550955](./img/1565796550955.png)

另一个则很正常

将一个参数，限流的行为

```java
flowRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
```

如果被限流了，那么采取的流量控制的行为（理解为线程池的 满了的拒绝策略）

* 直接拒绝（默认情况）
* warm up 预热。不会一下到达峰值，而是缓慢到达
* 均匀排队



### 分布式限流

以上的例子，如果我们配置两个服务，那么这个原本限流的数量为10，会变成20，这也是一种扩容的方式，我们配置的更多，能够承担的容量也就越多。这是集群。

但是，分布式却相反，明明分布在不同地方，却还是看起来是一个整体，对于这样的限流，我们首先想到了是zk中的分布式锁，zk的分布式锁是依靠zk中的有序节点去实现的，说到底是依靠第三方服务，对于sentinel的分布式限流也同样是一个道理，我们需要开发一个类似token的一个服务，来专门管理限流的方法，同时我们为了可以动态管理限流规则，我们也可以将规则配置到nacos中。但是为了高可用，我们将其他服务业配置的同样可以连接nacos，这样避免了token服务挂掉后的可用性问题。

那么现在我们来开发这样一个token服务，来管控各个服务之间的限流情况。

```xml
<!--遗憾的是，sentinel并没有提供现成的组件给我们，而是提供了api-->		
<dependency>
            <groupId>com.alibaba.csp</groupId>
            <artifactId>sentinel-cluster-server-default</artifactId>
            <version>1.6.3</version>
        </dependency>

<!--由于我们用到了nacos，这是nacos与sentinel的整合包,数据源规则-->

        <dependency>
            <groupId>com.alibaba.csp</groupId>
            <artifactId>sentinel-dadasource-nacos</artifactId>
            <version>1.6.3</version>
        </dependency>

<dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>1.7.26</version>
        </dependency>
```

关于日志额外说一句，在resources文件夹下，创建一个log4j.properties文件，加入如下内容。

```properties
log4j.rootLogger=info, stdout
log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%d %p [%c] - %m%n
```

加入服务的启动类，这些是提供sentinel提供给我们的api

```java
public class ClusterServer {

    public static void main(String[] args) throws Exception {

        ClusterTokenServer tokenServer = new SentinelDefaultTokenServer();

        ClusterServerConfigManager.loadGlobalTransportConfig(
                new ServerTransportConfig().setIdleSeconds(600).setPort(9999)
        );

        ClusterServerConfigManager.
                loadServerNamespaceSet(Collections.singleton("App-Pop"));

        tokenServer.start();
        //启动了一个token服务
    }

}
```

然后配置nacos的数据源

```java
public class DataSourceInitFunc implements InitFunc {

   private final String remoteAddress="192.168.216.1";//nacos host
   private final String groupId = "SENTINEL_GROUP";
   private final String FLOW_POSTFIX="-flow-rules";//dataid

    //意味着会从当前token-server会从nacos上获得限流规则
    @Override
    public void init() throws Exception {
        //这里的namesapce其实是我们之前设置的 App-Pop ，同时，这个还支持设置多个命名空间
        ClusterFlowRuleManager.setPropertySupplier(namespace->{
            //从nacos的数据源获取
            ReadableDataSource<String, List<FlowRule>>
                    rds = new NacosDataSource<List<FlowRule>>(
                            remoteAddress,groupId,namespace+FLOW_POSTFIX,
                    source -> JSON.parseObject(source, new TypeReference<>()));
            return rds.getProperty();//得到配置属性
        });
    }
}
```

接着通过sentinel的api，让我们加载到这个配置类。

![1565804060013](./img/1565804060013.png)

接着启动。

