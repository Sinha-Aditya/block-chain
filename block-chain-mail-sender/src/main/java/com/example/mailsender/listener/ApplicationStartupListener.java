package com.example.mailsender.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupListener.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("Application started - scheduler should now be active");
        System.out.println("==========================================");
        System.out.println("APPLICATION STARTED - SCHEDULER SHOULD BE ACTIVE");
        System.out.println("==========================================");
        
        // Print all beans related to scheduling
        String[] schedulerBeanNames = event.getApplicationContext().getBeanNamesForType(org.springframework.scheduling.TaskScheduler.class);
        System.out.println("Found " + schedulerBeanNames.length + " TaskScheduler beans:");
        for (String beanName : schedulerBeanNames) {
            System.out.println("- " + beanName);
        }
    }
} 