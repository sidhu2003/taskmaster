package com.example.taskmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
@SpringBootApplication
@EnableWebMvc
public class TaskMasterApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskMasterApplication.class, args);
    }
}