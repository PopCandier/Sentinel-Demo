package com.pop.sentinel.sentinelprovider;

import com.pop.sentinel.SentinelService;
import org.apache.dubbo.config.annotation.Service;

/**
 * @author Pop
 * @date 2019/8/13 23:49
 */
@Service
public class SentinelServiceImpl implements SentinelService {
    @Override
    public String sayHello() {
        return "Hello Pop";
    }
}
