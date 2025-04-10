package com.example.mailsender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // This is crucial - make sure it's here
public class BlockChainMailSenderApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlockChainMailSenderApplication.class, args);
    }
} 