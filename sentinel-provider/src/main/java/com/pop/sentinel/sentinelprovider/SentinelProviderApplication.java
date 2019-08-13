package com.pop.sentinel.sentinelprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class SentinelProviderApplication {

    public static void main(String[] args) throws IOException {

        SpringApplication.run(SentinelProviderApplication.class, args);
        System.in.read();
    }

}
