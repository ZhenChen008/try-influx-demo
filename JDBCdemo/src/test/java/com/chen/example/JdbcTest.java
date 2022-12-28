package com.chen.example;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JdbcTest {

    private   InfluxDB influxDB;

    @Before
    public void getConn(){
        // 获取连接
        influxDB = InfluxDBFactory.connect("http://localhost:8086", "root", "root");
        //设置 使用哪个数据库
        influxDB.setDatabase("myTimeSeriesDB");
    }

    @After
    public void closeConn(){
        // 关闭连接
        influxDB.close();
    }

    /**
     * 库的基本操作 创建一个库  使用一个库   删除一个库
     * 已经创建了数据库 myTimeSeriesDB
     */
    @Test
    public void testDataBase(){
        // 第二个参数使用哪个数据库
        QueryResult show_measurements = influxDB.query(new Query("show measurements","myTimeSeriesDB"));
        //返回 类型 QueryResult，打印出结果
        System.out.println(show_measurements);
    }

    @Test
    public void testSelection(){
        // 第二个参数使用哪个数据库
        QueryResult show_measurements = influxDB.query(
                new Query("select * from test22","myTimeSeriesDB"));
        //返回 类型 QueryResult，打印出结果
        System.out.println(show_measurements);
    }


}
