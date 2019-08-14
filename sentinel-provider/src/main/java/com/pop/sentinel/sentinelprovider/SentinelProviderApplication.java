package com.pop.sentinel.sentinelprovider;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.Collections;

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
        //请注意，这里拦截的是接口
        flowRule.setResource("com.pop.sentinel.SentinelService:sayHello()");
        flowRule.setCount(5);
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);

        //限定来源的请求
//        flowRule.setLimitApp("sentinel-consumer");

        FlowRuleManager.loadRules(Collections.singletonList(flowRule));
    }

}
