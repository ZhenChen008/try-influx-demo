package com.chen.example;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JdbcTest {

    private   InfluxDB influxDB;

    @Before //这个会在所有@Test之前执行
    public void getConn(){
        // 获取连接
        influxDB = InfluxDBFactory.connect("http://IP:8086", "root", "root");
        //设置 使用哪个数据库    //        influxDB.setDatabase("myTimeSeriesDB");
        influxDB.setDatabase("loudi");
    }

    @After   //这个会在所有@Test之后执行
    public void closeConn(){
        // 关闭连接
        influxDB.close();
    }


    /**
     *      库的基本操作 创建一个库  使用一个库   删除一个库
     *      已经创建了数据库 myTimeSeriesDB
     */
    @Test
    public void testDataBase(){
        // 需要填写第二个参数，设置使用哪个数据库
        QueryResult show_measurements = influxDB.query(new Query("select * from test","myTimeSeriesDB"));
       // influxDB.query(new Query("XXX","XX",true)) 第三个参数 true ----- 使用 POST方式

        //返回 类型 QueryResult，打印出结果
        System.out.println(show_measurements);
    }


    @Test
    public void testSelection(){

        QueryResult show_measurements = influxDB.query(
                new Query("show databases","myTimeSeriesDB"));
        //返回 类型 QueryResult，打印出结果
        System.out.println("打印数据库结果"+show_measurements);

//关于怎么 解析返回结果
        // 首先 获取 results
        List<QueryResult.Result> results = show_measurements.getResults();
        //第二步
        results.forEach(Result ->{
            List<QueryResult.Series> series = Result.getSeries();
            System.out.println("查看series="+series);
            System.out.println("查看它的第一个元素="+series.get(0));
            //System.out.println("查看它的第一个元素="+series.get(1)); // 报错 ，只有一个元素

            System.out.println("查看 元素values = "+series.get(0).getValues());
            //然后遍历打印
            series.get(0).getValues().forEach(x -> System.out.println(x));
        });

    }


    /**     表的基本操作 查询有哪些表  删除一个库
     *  首先需要选定使用哪个 数据库 ———— myTimeSeriesDB
     *  列出 myTimeSeriesDB 所含有的 Tables
     */
    @Test
    public void testTable(){
        QueryResult tablequery = influxDB.query(new Query("show measurements", "myTimeSeriesDB"));
        //返回 类型 QueryResult，打印出结果
        System.out.println(tablequery);
        // 首先获取 results，是一个列表 可以遍历
        List<QueryResult.Result> results = tablequery.getResults();

        //然后 遍历，首先打印看看，发现可以  getSeries
        results.forEach(everyone ->{
//            System.out.println(everyone);
            List<QueryResult.Series> Series = everyone.getSeries();
            System.out.println("Series="+Series);
        //再接着 遍历 Series
            Series.forEach(everyone2 ->{
                System.out.println(everyone2);
                // get获取 values
                List<List<Object>> values = everyone2.getValues();
                System.out.println("--- 列出 myTimeSeriesDB 所含有的 Tables---");
                values.forEach(System.out::println);
            });
        });

        //删除表 CPUInfo_Table ，谨慎删除，已经确认无用才删
        influxDB.query(new Query("drop MEASUREMENT CPUInfo_Table", "myTimeSeriesDB"));
    }


    /**     插入和 查询 基本操作 1
     *       创建 test 表测试
     *
     *  1- 插入数据 单条插入    influxDB.write(Point point) 还可以指定哪个DB，什么样的保留策略
     *
     *  2- 插入数据 插入一批数据   influxDB.write( BatchPoints.database("myTimeSeriesDB").build() .point(point_k)  );
     *
     */
    @Test
    public void testBasicCRUD01(){

        //首先创建   Point point , 指定时间精确到 微秒 TimeUnit.MICROSECONDS
        Point build_point1 = Point.measurement("test")
                .tag("tags-1", "yellow")
                .tag("tag-2", "Site02")
                .addField("wind风力", 5.0)
                .addField("water水量", 128L)
//                .time(new Date().getTime(), TimeUnit.MICROSECONDS).build();
                .addField("备注","不指定time").build();

        //单条插入,需要先定义 Point    influxDB.write(Point point)
        influxDB.write(build_point1);

        // 定义 BatchPoints 一批数据
        BatchPoints batchpoints01 = BatchPoints.database("loudi").build();

        // 延时 1 ms,定义单条数据 Point ,将单条 Point存储到集合中
        for (int k =999;k>0;k--){
            try {
                Thread.sleep(1);// 延时 1 ms
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("延时出现了问题啊！");
            }

            Point point_k = Point.measurement("test")       //这里设置 插入的数据表
                    .tag("tags-1", "yellow")
                    .tag("tag-2", "Site02")
                    .addField("wind风力", k-5.0)
                    .addField("water水量", 128L-k)
                    .addField("备注","批量插入的"+(10-k))
                    .time(new Date().getTime(), TimeUnit.MILLISECONDS).build();
            batchpoints01.point(point_k) ; //将单条数据存储到集合中
        }

        System.out.println(batchpoints01);
        //批量插入,需要先定义 BatchPoints, 然后  influxDB.write(batchPoints);
        influxDB.write(batchpoints01); ///将这一批次数据一次写入influxdb

    }


    /**         插入和 查询 基本操作
     *     创建 test 表测试
     *     注意：series 概念：time 和 tags 一起确定数据series,这个相同则会修改数据（更新）
     *
     *  a) 查询所有数据,暂时设置分页看看
     *     分页暂时还有点 问题—————— "limit 20,20 " 里面的 ,会报错 ————已经解决
     *     使用  LIMIT '多少条' OFFSET (page - 1)*rows    ,  OFFSET 前面参数是总条数，后面则是 起点
     *
     *
     *  b) 解析返回 结果，最终实现效果如下：
     *             可以获取 列名 columns  =   [time, tag-2, tags-1, water水量, wind风力, 备注]
     *             实现拎出 每一条数据   point_10 =   [1.672288702846E12, Site02, yellow, 108.0, 16.0, null]
     */
    @Test
    public void testBasicCRUD02(){
        // 查询所有数据,暂时设置分页看看
        QueryResult table_test = influxDB.query(
                new Query("select * from test LIMIT 20 OFFSET 6;", "loudi"), TimeUnit.MICROSECONDS);
        System.out.println(table_test);

        // 01-拿到 results
        List<QueryResult.Result> results = table_test.getResults();
        //02- 遍历 results 里面的
        results.forEach(Result ->{
            //03- 获取里面的 series, 可以先 打印看看 series
            List<QueryResult.Series> series = Result.getSeries();
            System.out.println("--------可以先 打印看看 series--------");
            System.out.println(series);

            //04-遍历 列表 series，关注里面的
            series.forEach(x ->{
                // 5-1 :拿到里面的 列名称 columns
                List<String> columns = x.getColumns();
                // 5-2 :拿到里面的 列对应的值 values
                List<List<Object>> values = x.getValues();
                System.out.println("打印 列名称 columns、列对应的值 values 看看");
                System.out.println(columns);
//                values.forEach(System.out::println);

                // 06-获取 values里面的每一个数据 point(也是列表)
                System.out.println("打印每一个点 Point!");
                for (int i = 0; i < values.size(); i++) {
                    List<Object> point_i = values.get(i);
                    System.out.println(point_i);
                }

                // 07-打印列名称和对应的一条数据———————— OK
                System.out.println("第十个点 的数据： "+columns);
                System.out.println(values.get(10));

            });
        });
    }


    /**         保留策略 ———创建了一个新的库 loudi  和 30天的保存时间,默认使用RP策略 rp_30_days
     *  1-查看指定数据库的 保留策略
     *  2- 添加 新的保留策略 并且使用它 修改 保留策略 rp_30_days、autogen之间切换 ————经过测试切换 不会马上删除原来数据,但是需要加上 原来策略名 查询
     *
     *  3- PS : 对于retention policy，最好的方式是在创建数据库时就考虑清楚数据要保留多长时间。
     */
    @Test
    public void testRetentionPolicy() {
        //1.选择一个库   influxDB.setDatabase("history");
        //2.查询当前库策略
        QueryResult queryResult = influxDB.query(new Query("show retention policies", "loudi"));
        System.out.println(queryResult);

        influxDB.query(
                new Query("create retention policy rp_30_days on loudi duration 30d replication 1  default "
                        ,"loudi"));
        // 修改 保留策略 rp_30_days、autogen之间切换
        influxDB.query(new Query("ALTER RETENTION POLICY rp_30_days ON loudi default","loudi"));

        // 删除策略 drop retention policy Week_7days on loudi
//        influxDB.query(new Query("drop retention policy Week_7days on loudi","loudi"));
    }
}

