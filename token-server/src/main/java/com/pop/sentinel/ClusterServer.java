package com.pop.sentinel;

import com.alibaba.csp.sentinel.cluster.server.ClusterTokenServer;
import com.alibaba.csp.sentinel.cluster.server.SentinelDefaultTokenServer;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;

import java.util.Collections;

/**
 * @author Pop
 * @date 2019/8/15 0:17
 */
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
