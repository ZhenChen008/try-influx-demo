package com.chen.example.service;

import com.chen.example.config.InfluxDBTemplate;
import com.chen.example.entity.Firsttext;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class FirsttextServiceImpl implements FirsttextService {

    List <Firsttext> point_firstList = new ArrayList<>();

    private final InfluxDBTemplate influxDBTemplate;
    private final String Table_Name ="test"; // 代替表名称

    @Autowired  // 实际上 推荐使用构造器注入
    public FirsttextServiceImpl(InfluxDBTemplate influxDBTemplate) {
        this.influxDBTemplate = influxDBTemplate;
    }


    @Override
    public void save(Firsttext forecast) {
        //写入信息，首先可以确定 表明，然后是 tag、time、field
        influxDBTemplate.write(Table_Name,
                forecast.getTags(),
                forecast.getFields(),
                forecast.getTime(),
                TimeUnit.MILLISECONDS);

    }

    @Override
    public List<Firsttext> findAll() {
        List<Firsttext> firsttextList = new ArrayList<>(); // 后面添加用于保存值的


        //分页查询最后的 ，LIMIT <= 30条
        QueryResult testTableResult = influxDBTemplate.query("select * from test limit 30 OFFSET 990;", "loudi");

        List<Firsttext> firsttextListBack = handleQueryResult(testTableResult, Firsttext.class);
        // 以下代码 可以封装成上面一句

/*
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
        return firsttextListBack;
    }




}
