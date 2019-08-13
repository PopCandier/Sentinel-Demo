package com.pop.sentinel.sentinelconsumer.sentinelcosumer;

import com.pop.sentinel.SentinelService;
import org.apache.dubbo.config.annotation.Reference;
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
        return sentinelService.sayHello();
    }
}
