package com.chen.example.controller;

import com.chen.example.entity.Firsttext;
import com.chen.example.entity.TestPoint;
import com.chen.example.service.FirsttextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController                     // 标记 是控制类
@RequestMapping("/influxTest")      // 请求头

public class HelloController {

    @Autowired
    private FirsttextService forecastService;   //注入服务层

    @GetMapping("/hello")
    public String hello(){
        return "hello 2023  Welcome to MDB-Chen-influxDB!";
    }

    @GetMapping("/insertOne")
    public String testSave(){

        Firsttext firsttext = new Firsttext();

        //赋值 tags \fields \time   System.currentTimeMillis();
            firsttext.setTime(  System.currentTimeMillis()); // 赋值时间
        firsttext.getTags().put("tags-1","Blue"); // Tags 赋值
        firsttext.getTags().put("tag-2","HongKong");
        firsttext.getFields().put("water水量",28); // 注意 FieldtypeConflictException
        firsttext.getFields().put("wind风力",20.23);
        firsttext.getFields().put("备注","OOP方式添加");

        System.out.println("//打印看看 \n"+firsttext);
        // 调用 执行，以面向对象的方式，先在上面 赋值
        forecastService.save(firsttext);
        return  "firsttext 插入数据成功！";
    }

    @GetMapping("/findAll")
    public List<Firsttext>  selectAllTest(){
        // 直接 调用 Service 的 查询功能 findAll()
        List<Firsttext> selectAll = forecastService.findAll();
        System.out.println("查询返回的结果是:");
        selectAll.forEach(System.out::println);
        return selectAll;
    }


    @GetMapping("/insertByPojo")
    public String insertByPojo() {

        //以对象形式传入参数
        TestPoint testPoint = new TestPoint(Instant.now(),
                "Changsha", "Orange",
                63, 63.48, "POJO写入 Test2");
        forecastService.insertByPojo(testPoint);
        return "Insert by Pojo success!";
    }

    @GetMapping("/findAllByMapper")
    public List<TestPoint>  selectAllByMapper(){
        List<TestPoint> pointList = forecastService.selectAllByMapper();
        System.out.println(" 使用了 InfluxDBResultMapper 执行 findAllByMapper查询返回的结果是: ");
        pointList.forEach(System.out::println);
        return pointList ;
    }

}