package com.chen.example;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication //表示这个类 是springboot主启动类。
public class JDBCApplication {

    public static void main(String[] args) {
        SpringApplication.run(JDBCApplication.class, args);
    }
}
