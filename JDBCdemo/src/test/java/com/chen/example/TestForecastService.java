package com.chen.example;

import com.chen.example.entity.Firsttext;
import com.chen.example.service.FirsttextService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@SpringBootTest(classes = JDBCApplication.class)
@RunWith(SpringRunner.class)
public class TestForecastService {

    @Autowired
    private FirsttextService forecastService;   //注入服务层

    @Test
    public void testSave(){

        Firsttext firsttext = new Firsttext();

        //赋值 tags \fields \time

        firsttext.setTime( new Date().getTime()); // 赋值时间

        firsttext.getTags().put("tags-1","Blue"); // Tags 赋值
        firsttext.getTags().put("tag-2","HongKong");

        firsttext.getFields().put("water水量",28); // 注意 FieldtypeConflictException
        firsttext.getFields().put("wind风力",20.23);
        firsttext.getFields().put("备注","OOP方式添加");

        // 调用 执行，以面向对象的方式，先在上面 赋值
        forecastService.save(firsttext);
        System.out.println("firsttext 插入数据成功！");


    }

    @Test
    public void testSelection(){

        // 直接 调用 Service 的 查询功能 findAll()
        List<Firsttext> selectAll = forecastService.findAll();
        System.out.println("查询返回的结果是:");
        selectAll.forEach(System.out::println);

    }
}
