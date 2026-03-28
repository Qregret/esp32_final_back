package com.example.smartlab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.example.smartlab.mapper")
@EnableScheduling
public class SmartLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartLabApplication.class, args);
    }
}
