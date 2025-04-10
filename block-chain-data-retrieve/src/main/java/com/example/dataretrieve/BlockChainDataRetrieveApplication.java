package com.example.dataretrieve;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class BlockChainDataRetrieveApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlockChainDataRetrieveApplication.class, args);
    }
} 