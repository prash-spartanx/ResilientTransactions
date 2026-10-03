
package com.prashant.transaction.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();

        // Single server mode (most common)
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setPassword(null) // set if your Redis requires auth
                .setConnectionPoolSize(64)
                .setConnectionMinimumIdleSize(24);

        return Redisson.create(config);
    }
}
