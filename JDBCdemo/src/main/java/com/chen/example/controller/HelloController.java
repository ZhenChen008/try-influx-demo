package com.chen.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController // 标记 是控制类
@RequestMapping("/JDBCdemo")  // 请求头

public class HelloController {

    @GetMapping("/hello")
    public String hello(){
        return "hello 2023 Git Welcome!";
    }
}