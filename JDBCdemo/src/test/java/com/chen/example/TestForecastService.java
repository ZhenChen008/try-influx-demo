package com.chen.example;

import com.chen.example.config.InfluxDBTemplate;
import com.chen.example.entity.Firsttext;
import com.chen.example.entity.TestPoint;
import com.chen.example.service.FirsttextService;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBMapper;
import org.influxdb.impl.InfluxDBResultMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

// 模拟一个springboot项目的设置
@SpringBootTest(classes = JDBCApplication.class)
@RunWith(SpringRunner.class)
public class TestForecastService {

    @Autowired
    private FirsttextService forecastService;   //注入服务层

    //从配置文件中取值
    @Value("${influxdbconninfo.url}")
    String serverURL;
    @Value("${influxdbconninfo.username}")
    String username;
    @Value("${influxdbconninfo.password}")
    String password;
    @Value("${influxdbconninfo.database}")
    String databaseName;
    @Value("${influxdbconninfo.retention}")
    String retentionpolicy;

    @Test
    public void testSave2023(){
        System.out.println("----------------- 测试 InfluxDBResultMapper -----------------  ");

        System.out.println("首先 获取一个连接,使用 @value 从yml文件里面注入：");
        InfluxDB influxDB = InfluxDBFactory.connect(serverURL, username, password);
        influxDB.setDatabase(databaseName);
        // 设置日志级别 BASIC
        influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);  // NONE、 BASIC、HEADERS 打印更加详细

        // 这个之后看源码了解一下使用 怎么设置 InfluxDBMapper influxDBMapper = new InfluxDBMapper(influxDB);

        TestPoint point1 = new TestPoint();     //定义一个 point对象
//        看看怎么使用 使用 POJO 写作
//        就像我们用来将数据转换为 POJO 一样，我们可以将数据写入 POJO。 具有相同的 POJO 类 CPU
        point1.setTime(Instant.now());         // ... setting data
        // 也可以 赋值 System.currentTimeMillis(), TimeUnit.MILLISECONDS
        point1.setTagOne("Blue");
        point1.setTagTwo("HongKong");
        point1.setWaterVol(28);
        point1.setWindForce(2023.14);
        point1.setNote("不执行close()");

//        看看  西交 数据集

        TestPoint point2 = new TestPoint(Instant.now(),
                "Changsha", "green", 56, 18.52, "POJO写入OK");

//        point1 = Point.measurementByPOJO(TestPoint.getClass()).addFieldsFromPOJO(TestPoint).build();
        Point build_point = Point.measurementByPOJO(TestPoint.class).addFieldsFromPOJO(point2).build();
        System.out.println("看看点 build_point ："+build_point);
        influxDB.write("loudi","rp_30_days",build_point);
//  这个是错误的写法     influxDB.write(build_point);


//        Dao层
        QueryResult queryResult = influxDB.query(new Query("SELECT * FROM test2023 LIMIT 20 "));


//        服务层 impl
        System.out.println("之前输出打印的：\n"+queryResult);
        // 首先 查询一下 数据库表 返回结果

        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper(); // thread-safe - can be reused
        List<TestPoint> testPoints = resultMapper.toPOJO(queryResult, TestPoint.class);

//        controller
        System.out.println("之后输出打印的：\n"+testPoints);
//        influxDBMapper  问题。似乎使用write不是使用influxDBMapper的方法，而是应该使用influxDBMapper.save（）。
    }


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

        System.out.println("//打印看看 \n"+firsttext);
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


