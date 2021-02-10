package com.samchenjava.mall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration//n.159 315
public class MyRedissonConfig {

    @Bean(destroyMethod = "shutdown")
    RedissonClient redisson() throws IOException {
        Config config = new Config();
        //in our project, we select single server mode
        config.useSingleServer().setAddress("redis://192.168.0.16:6379");
        return Redisson.create(config);
    }
}
