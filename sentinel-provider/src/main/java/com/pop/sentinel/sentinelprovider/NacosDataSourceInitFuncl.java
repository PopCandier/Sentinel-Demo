package com.pop.sentinel.sentinelprovider;

import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pop
 * @date 2019/8/16 23:12
 * sentienl中熔断的时间
 */
public class NacosDataSourceInitFuncl implements InitFunc {
    @Override
    public void init() throws Exception {
        List<DegradeRule> rules = new ArrayList<>();
        DegradeRule rule = new DegradeRule();
        /*
        * 熔断无法定义在方法级别，只能定义在类级别
        * */
        rule.setResource("com.pop.sentinel.SentinelService");

        // 定义我们规则
        /**
         * 对于类型来说有三种
         * 1 RT 就是平均响应时间
         * 2 错误比率
         * 3 错误次数
         *
         * 这里就是1s内请求5个请求
         */
        rule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        /**
         * 这里的count含义取决于上一步的设置，例如，我们在上一步
         * 选择了RT 那么意味着我们在这里设置的count的含义，
         * 每个请求平均的响应的时间(ms)
         *
         * 也就是说，如果平均每次请求处理5个，那么5个的请求的平均响应
         * 时间不能超过10ms，不染触发熔断机制
         */
        rule.setCount(10);
        rule.setTimeWindow(5);//时间窗口，表示5秒后可用。
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);

    }
}
