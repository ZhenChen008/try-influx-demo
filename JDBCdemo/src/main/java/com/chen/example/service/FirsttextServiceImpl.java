package com.chen.example.service;

import com.chen.example.config.InfluxDBTemplate;
import com.chen.example.entity.Firsttext;
import com.chen.example.entity.TestPoint;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class FirsttextServiceImpl implements FirsttextService {


    private final InfluxDBTemplate influxDBTemplate;
    private final String Table_Name ="test2023"; // 代替表名称

    @Autowired  // 实际上 推荐使用构造器注入
    public FirsttextServiceImpl(InfluxDBTemplate influxDBTemplate) {
        this.influxDBTemplate = influxDBTemplate;
    }



    @Override
    public void save(Firsttext forecast) {
/*
        Point.Builder builder = Point.measurement(Table_Name);

        builder.tag(forecast.getTags());
        builder.fields(forecast.getFields());
        builder.time(forecast.getTime(), TimeUnit.MILLISECONDS);
        Point data_point = builder.build();

*/
        //写入信息 参数，首先可以确定 表名，然后是 tag、time、field
        influxDBTemplate.write( Table_Name,
                forecast.getTags(),forecast.getFields(),forecast.getTime(),TimeUnit.MILLISECONDS);
    }


    @Override
    public List<Firsttext> findAll() {

        List<Firsttext> firsttextList1 = influxDBTemplate.selectQuery(
                                                        "select * from test limit 30 OFFSET 990;",
                                                        //分页查询最后的 ，LIMIT <= 30条
        // limit size offset N： 	size表示每页 大小，N表示第几条记录开始查询
                                                        Firsttext.class);
        return  firsttextList1;

/*
        //分页查询最后的 ，LIMIT <= 30条
        QueryResult testTableResult = influxDBTemplate.query("select * from test limit 30 OFFSET 990;", "loudi");

        List<Firsttext> firsttextListBack = handleQueryResult(testTableResult, Firsttext.class);
        // 以下代码 可以封装成上面一句


        List<QueryResult.Result> results = testTableResult.getResults();
        results.forEach( result -> {
            List<QueryResult.Series> seriesList = result.getSeries();
            seriesList.forEach( x->{  // 两次 遍历
                // 获取 values 、列名称
                List<String> columns = x.getColumns();
                List<List<Object>> values = x.getValues();
//                System.out.println("\n\n\n打印值 values 看看\n ");
//                values.forEach(System.out::println);
                for(int i=0;i<values.size();i++){   //遍历 values
                    System.out.println("一行数据： "+values.get(i).get(0));
                    //然后遍历 values,columns拿出来数据
                    //后面添加的 创建 临时保存对象 一个Point，一个个插入 firsttextList
                    Firsttext point_x = new Firsttext();

                    for(int j=0 ;j<columns.size();j++)
                    {
                        String column_i = columns.get(j);
                        Object value_i = values.get(i).get(j);
                        if("time".equals(column_i)){
                            point_x.setTime(Timestamp.from(ZonedDateTime.parse(String.valueOf(value_i)).toInstant()).getTime()); //时间转换
                        }
                        //上面是单独处理时间，下面是统一处理 filed =tags+field
                        else {
                    //注意: 返回结果无须在知道是 tags 还是 fields  认为就是字段和值 可以将所有字段作为 field 进行返回
//                            if( ObjectUtils.isEmpty(value_i)){}
                            point_x.getFields().put(column_i,value_i);
                        }
                    }

                    // 添加进去 List ,上面是 给 point 赋值
                    firsttextList.add(point_x);

                }
            });
        });

        //关于如何封装 数据，查看 实战应用的第二节


//        List<QueryResult.Result> results = loudiResult.getResults();
//        results.forEach(x ->{
//            List<QueryResult.Series> seriesList = x.getSeries();
//            seriesList.forEach( y ->{
//                List<String> columns = y.getColumns();// 获取所有的 列 Columns
//                List<List<Object>> values = y.getValues();//获取所有的 values
//
//                //然后遍历 values,columns拿出来数据
//                //关于如何封装 数据，查看 实战应用的第二节
//        //每一次 遍历values ,创建一个 point 对象 ，并且放入到 列表中
//                Firsttext firsttexts = new Firsttext();
//                point_firstList.add(firsttexts);
//
//                //实战应用的第二节: 返回的结果 不分字段，全部作为 field 取出
//
//            });
//
//        });

        return firsttextList;*/
    }

    // 方法-3 插入数据（使用 POJO的方式）
    @Override
    public void insertByPojo(TestPoint testPoint) {

/*        // ... setting data
        point.setTime(Instant.now());
        // 也可以 赋值 System.currentTimeMillis(), TimeUnit.MILLISECONDS
        point.setTagOne("red");
        point.setTagTwo("HongKong");
        point.setWaterVol(28);
        point.setWindForce(2023.14);
        point.setNote("use MVC in Service");*/
        //influx write处理
        Point build_point = Point.measurementByPOJO(TestPoint.class).addFieldsFromPOJO(testPoint).build();
        System.out.println("看看 input = build_point ："+build_point);

        // 调用 write 给 Dao层传入一个 point对象
        influxDBTemplate.write(build_point);

    }


    /**     方法-4 查询全部并且使用Mapper
     *       List<TestPoint> selectAllByMapper();
     *
     * @return
     */
    @Override
    public List<TestPoint> selectAllByMapper() {
        QueryResult queryResult = influxDBTemplate.query(
                "SELECT * FROM test2023 LIMIT 20 ", "loudi");

        // 需要从 influxDBTemplate 返回一个 QueryResult ,所以需要上面的代码
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper(); // thread-safe - can be reused
        List<TestPoint> pointList = resultMapper.toPOJO(queryResult, TestPoint.class);
        return pointList;
    }


    // 方法-5    批量写入数据 -1
    @Override
    public void insertByBatch(BatchPoints batchPoints) {
        influxDBTemplate.writeBatch(batchPoints);
    }

    // 方法-6    批量写入数据 -2
    @Override
    public void batchLinesInsert( List<String> batchRecords) {
        influxDBTemplate.batchLinesInsert(batchRecords);
    }
}
