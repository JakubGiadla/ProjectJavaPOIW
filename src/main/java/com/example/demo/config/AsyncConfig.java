package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);        // 5 wątków na start
        executor.setMaxPoolSize(10);        // Max 10 wątków
        executor.setQueueCapacity(100);     // Kolejka zadań
        executor.setThreadNamePrefix("async-mail-");
        executor.initialize();
        return executor;
    }
}