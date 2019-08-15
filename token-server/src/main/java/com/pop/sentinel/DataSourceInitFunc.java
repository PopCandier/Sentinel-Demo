package com.pop.sentinel;

import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.List;

/**
 * @author Pop
 * @date 2019/8/15 0:50
 *
 * 从nacos中获得动态数据源
 */
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
                    source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>(){}));
            return rds.getProperty();//得到配置属性
        });
    }
}
