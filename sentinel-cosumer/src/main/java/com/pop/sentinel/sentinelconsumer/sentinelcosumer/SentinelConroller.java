package com.pop.sentinel.sentinelconsumer.sentinelcosumer;

import com.pop.sentinel.SentinelService;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Pop
 * @date 2019/8/14 0:30
 */
@RestController
public class SentinelConroller {

    @Reference
    SentinelService sentinelService;

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
}
