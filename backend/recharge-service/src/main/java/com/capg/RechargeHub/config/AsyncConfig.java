package com.capg.RechargeHub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.Executor;

/**
 * Async configuration enabling parallel execution of service calls.
 * Configures a dedicated thread pool for @Async methods.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "rechargeTaskExecutor")
    public Executor rechargeTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("RechargeAsync-");
        
        executor.setTaskDecorator(new ContextCopyingDecorator());
        
        executor.initialize();
        return executor;
    }

    static class ContextCopyingDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            RequestAttributes context = RequestContextHolder.getRequestAttributes();
            return () -> {
                try {
                    if (context != null) {
                        RequestContextHolder.setRequestAttributes(context);
                    }
                    runnable.run();
                } finally {
                    RequestContextHolder.resetRequestAttributes();
                }
            };
        }
    }
}
