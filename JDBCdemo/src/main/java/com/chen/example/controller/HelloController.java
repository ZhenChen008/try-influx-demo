package com.chen.example.controller;

import com.chen.example.entity.Firsttext;
import com.chen.example.entity.TestPoint;
import com.chen.example.service.FirsttextService;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
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
        firsttext.getFields().put("备注","try again");

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

    @PostMapping("/batchInsert")         // 批量 插入
    public String insertPointBatch() {

        BatchPoints batchPoints2 = BatchPoints.database("loudi").retentionPolicy("rp_30_days").build();
        // 加入具体的点
        //以对象形式传入参数
        TestPoint testPoint = new TestPoint(Instant.now(),
                            "Changsha", "Orange",
                            63, 63.48, "插入batch_03");
        TestPoint testPoint_2 = new TestPoint(Instant.now(),
                            "Changsha", "Orange",
                            99, 92.35, "插入batch_04plus");

        Point build_point = Point.measurementByPOJO(TestPoint.class)
                .addFieldsFromPOJO(testPoint).build();
        Point build_point_2 = Point.measurementByPOJO(TestPoint.class)
                .addFieldsFromPOJO(testPoint_2).build();
/*
//        Builder builder = Point.measurement(measurement); 结合 builder.build());

参考
        https://github.com/influxdata/influxdb-java/blob/influxdb-java-2.23/
        // MANUAL.md#:~:text=2%20DEFAULT%22))%3B-,
        // BatchPoints%20batchPoints%20%3D%20BatchPoints,-.database(
*/
        batchPoints2.point(build_point);      //将 point放入batch中
        batchPoints2.point(build_point_2);    //将 point放入batch中

        //将这一批次数据一次写入influxdb
        forecastService.insertByBatch(batchPoints2);
        return "Insert by batch OK!";
    }


    /**  建议 了解一下这个 ————（调用BatchPoints.lineProtocol()可得到一条record）
     * 批量写入数据
     *
     * @param
     *  （调用BatchPoints.lineProtocol()可得到一条record）

    public void batchLinesInsert( List<String> records) {
        influxDB.write(database, retentionPolicy, consistency, records);
    }
     */
    @PostMapping("/insertLines")         // 批量 插入，按照行协议
    public String insertLineProtocol() {
        List<String> lines_protcol=new ArrayList<>();
//        lines_protcol.add("test2023,tag-2=serverA,tags-1=Orange water水量=66i,wind风力=12.58,备注=\"line protl\" ");
        lines_protcol.add("test2023,tag-2=serverB,tags-1=Orange water水量=66i,wind风力=12.88,备注=\"换行插入3\"\n test2023,tag-2=serverC,tags-1=green water水量=99i,wind风力=123.88,备注=\"换行插入4\"");

        System.out.println("---     执行 按照行协议插入    ---");
        forecastService.batchLinesInsert(lines_protcol);
        return "Insert insertLineProtocol  OK!";
    }


}