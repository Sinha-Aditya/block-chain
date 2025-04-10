package com.example.mailsender.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler());
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("scheduler-task-");
        scheduler.setErrorHandler(t -> {
            System.err.println("Scheduler error occurred: " + t.getMessage());
            t.printStackTrace();
        });
        scheduler.initialize();
        return scheduler;
    }
    
    // Simple test task to verify scheduling works
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void testScheduler() {
        System.out.println("TEST SCHEDULER PING - " + System.currentTimeMillis());
    }
} 