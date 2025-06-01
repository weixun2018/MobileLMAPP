package com.example.bluecat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.example.bluecat.mapper")
public class BlueCatServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlueCatServerApplication.class, args);
    }
} 