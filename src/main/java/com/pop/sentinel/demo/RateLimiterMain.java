package com.pop.sentinel.demo;

import com.google.common.util.concurrent.RateLimiter;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * @author Pop
 * @date 2019/8/12 22:42
 */
public class RateLimiterMain {

    // 令牌桶的实现， 单机进程
    RateLimiter rateLimiter = RateLimiter.create(10);//每次生成10个令牌

    public void doTest(){
        if(rateLimiter.tryAcquire()){//这里获得一个令牌
            System.out.println("允许通过进行访问");
        }else{
            System.out.println("被限流了");
        }

    }

    public static void main(String[] args) throws IOException {
        RateLimiterMain main = new RateLimiterMain();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Random random = new Random();

        for(int i =0;i<20;i++){
            new Thread(()->{

                try {
                    countDownLatch.await();
                    Thread.sleep(random.nextInt(1000));
                    main.doTest();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }).start();
        }

        countDownLatch.countDown();
        System.in.read();

    }

}
