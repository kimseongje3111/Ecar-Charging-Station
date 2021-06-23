package com.ecar.servicestation.infra.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
@Slf4j
public class SchedulerConfig implements SchedulingConfigurer {

    private static final int POOL_SIZE = 10;
    private static final String SCHEDULER_PREFIX = "@schedule-task-";

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(POOL_SIZE);
        threadPoolTaskScheduler.setThreadNamePrefix(SCHEDULER_PREFIX);
        threadPoolTaskScheduler.setErrorHandler(t -> log.error("Error message : 'scheduler exception'"));
        threadPoolTaskScheduler.initialize();

        registrar.setTaskScheduler(threadPoolTaskScheduler);
    }
}
