package com.chen.example.entity;

import lombok.Data;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.annotation.TimeColumn;

import java.time.Instant;


/*
该类使用@Measurement(name = “memory”)进行注释，对应于我们用来创建Points的Point.measurement(“memory”)。

        对于 QueryResult中的每个字段，我们添加带有相应字段名称的@Column(name = “XXX”)注释。

        QueryResults使用InfluxDBResultMapper 映射到 POJO。

        InfluxDBMapper influxDBMapper = new InfluxDBMapper(influxDB);
*/

@Data     // getters and setters  、toString
@Measurement(name = "test2023")
public class TestPoint {

    // 映射 数据库表的字段是 Column
    @TimeColumn
    @Column(name = "time")
    private Instant time;

    // 注解中添加 tag = true,表示当前字段内容为 tag内容
    // 用 @Column（...， tag = true） 注释的类字段（即 InfluxDB Tag） 必须声明为 String。
    @Column(name = "tag-2", tag = true)
    private String tagTwo;
    @Column(name = "tags-1", tag = true)
    private String tagOne;


    @Column(name = "water水量")
    private Integer waterVol;
    @Column(name = "wind风力")
    private Double windForce;
    @Column(name = "备注")
    private String  Note;


    // 全参数构造器  和 无参构造器
    public TestPoint() {
    }
    public TestPoint(Instant time, String tagTwo, String tagOne, Integer waterVol, Double windForce, String note) {
        this.time = time;
        this.tagTwo = tagTwo;
        this.tagOne = tagOne;
        this.waterVol = waterVol;
        this.windForce = windForce;
        Note = note;
    }
}