package com.pop.sentinel.sentinelprovider;

import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
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

    //这是nacos的地址
   private final String remoteAddress="192.168.216.1";//nacos host
   private final String groupId = "SENTINEL_GROUP";
   private final String FLOW_POSTFIX="-flow-rules";//dataid

    //令牌服务器的host地址。
    private final String CLUSTER_SERVER_HOST ="localhost";
    //令牌服务器的端口号。
    private final int CLUSTER_SERVER_PORT=9999;
    //这里需要的是，如果设置短了，就会降级，这是去拿到令牌的等待时间。
    private final int REQUEST_TIME_OUT = 200000;//请求超时时间。 ms

    private final String APP_NAME = "App-Pop";//name -sapce

    @Override
    public void init() throws Exception {
        // 不再是获得集群的信息，而是获得加载的信
        loadClusterClientConfig();
        registryClusterFlowRuleProperty();
    }

    private void loadClusterClientConfig(){
        //获得集群加载客户端的配置
        ClusterClientAssignConfig assignConfig
                 = new ClusterClientAssignConfig();
        assignConfig.setServerHost(CLUSTER_SERVER_HOST);
        assignConfig.setServerPort(CLUSTER_SERVER_PORT);
        ClusterClientConfigManager.applyNewAssignConfig(assignConfig);

        ClusterClientConfig clientConfig = new ClusterClientConfig();
        clientConfig.setRequestTimeout(REQUEST_TIME_OUT);
        ClusterClientConfigManager.applyNewConfig(clientConfig);
    }

    //和token服务端一样，同样需要从nacos上获得集群限流配置
    //注册nacos的动态数据源
    private void registryClusterFlowRuleProperty(){
            //从nacos的数据源获取
            ReadableDataSource<String, List<FlowRule>>
                    rds = new NacosDataSource<List<FlowRule>>(
                    remoteAddress,groupId,APP_NAME+FLOW_POSTFIX,
                    source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>(){}));

        FlowRuleManager.register2Property(rds.getProperty());
        //没这一步，限流规则无法应用
    }
}
