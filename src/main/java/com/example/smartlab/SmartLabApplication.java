package com.example.smartlab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.smartlab.mapper")
public class SmartLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartLabApplication.class, args);
    }
}
