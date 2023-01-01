package com.chen.example.config;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class InfluxDBTemplate {


    //1.influxDBTemplate ---> influxdb---> 数据库 获取 influxdb 连接


    private final InfluxdbProperties influxdbProperties;

    private InfluxDB influxDB;

    @Autowired
    public InfluxDBTemplate(InfluxdbProperties influxdbProperties) {
        this.influxdbProperties = influxdbProperties;
        getInfluxDB();
    }

    /**     获取 influxdb 连接
     */
    public void getInfluxDB() {
        if (influxDB == null) {
            influxDB = InfluxDBFactory.connect(
                                                influxdbProperties.getUrl(),
                                                influxdbProperties.getUsername(),
                                                influxdbProperties.getPassword() );
            //设置使用数据库  保证库存在
            influxDB.setDatabase(influxdbProperties.getDatabase());
            //设置数据库保留策略 保证策略存在
            if (ObjectUtils.isEmpty(influxdbProperties.getRetention())) {
                influxDB.setRetentionPolicy(influxdbProperties.getRetention());
            }
        }
    }


    /**
     * 关闭连接
     */
    public void close() {
        if (influxDB != null) {
            influxDB.close();
        }
    }

    /**
     * 指定时间插入
     *
     * @param measurement 表
     * @param tags        标签
     * @param fields      字段
     * @param time        时间
     * @param unit        单位
     */
    public void write(String measurement, Map<String, String> tags, Map<String, Object> fields, long time, TimeUnit unit) {
        Point point = Point.measurement(measurement).tag(tags).fields(fields).time(time, unit).build();
        influxDB.write(point);
        close();
    }

    /**
     * 插入数据-自动生成时间
     *
     * @param measurement 表
     * @param tags        标签
     * @param fields      字段
     */
    public void write(String measurement, Map<String, String> tags, Map<String, Object> fields) {
        write(measurement, tags, fields, System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }


    /**
     * 用来执行相关操作
     *
     * @param command 执行命令
     * @return 返回结果
     */
    public QueryResult query(String command,String database) {
        return influxDB.query(new Query(command,database));
    }


    /**         select 查询封装处理   handleQueryResult(QueryResult res, Class<T> clazz )
     * @param queryResult  查询返回结果
     * @param clazz 封装的对象类型
     * @param <T>   泛型
     * @return   返回处理结果
     */
    public <T> List<T> handleQueryResult(QueryResult queryResult, Class<T> clazz )
    {// 参考第三节 视频 开头的
        System.out.println("\n\n调用工具类方法 handleQueryResult(QueryResult queryResult, Class<T> clazz )\n\n");
        // 定义 保存结果集合
        List<T> lists = new ArrayList<>();

        // 下面是 解析查询返回结果
        List<QueryResult.Result> results = queryResult.getResults();
        results.forEach( result -> {
            List<QueryResult.Series> seriesList = result.getSeries();
            seriesList.forEach( x->{  // 两次 遍历
                // 获取 values 、列名称
                List<String> columns = x.getColumns();
                List<List<Object>> values = x.getValues();
                //遍历 values
                for(int i=0;i<values.size();i++){
                    System.out.println("一行数据： "+values.get(i).get(0));
                    //然后遍历 values,columns拿出来数据
                    //后面添加的 创建 临时保存对象 一个Point，一个个插入 firsttextList
                    try {
                        //然后是给对象 T 属性赋值，通过反射 给对象赋值 getDeclatedConstructor().newInstance();
                        T instance = clazz.newInstance();
                        // 使用Spring的 BeanWrapper 反射赋值
                        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(instance);

                        // 定义一个 Map 记住  tags+field
                        HashMap<String ,Object> resultFields =new HashMap<>();

                        for(int j=0 ;j<columns.size();j++){
                            String column_i = columns.get(j);
                            Object value_i = values.get(i).get(j);

                            if("time".equals(column_i)){        // 这里 通过反射赋值
                                beanWrapper.setPropertyValue("time",
                                        Timestamp.from(ZonedDateTime.parse(String.valueOf(value_i)).toInstant()).getTime());
                                //时间转换
                            } else {       //上面是单独处理时间，下面是统一处理 filed =tags+field
                                resultFields.put(column_i,value_i);
                            }
                        }

                        System.out.println("    // 通过 反射完成 fields 赋值操作");
                        beanWrapper.setPropertyValue("fields",resultFields);
                        lists.add(instance);    // 添加进去 List ,上面是 给 point 赋值

                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        return  lists;
    }



    /**
     * @param selcetCommand  select查询语句
     * @param clazz         类型
     * @param <T>  泛型
     * @return     结果
     *
     *      在有了上面的那个 handleQueryResult(QueryResult queryResult, Class<T> clazz )
     *   之后，再进一步封装 查询函数 ,还依赖上面的 query()
     *
     */
    public <T> List<T> selectQuery(String selcetCommand, Class<T> clazz ){
        // 首先查询
        QueryResult queryResult = query(selcetCommand, "loudi");

        //然后调用查询处理方法 handleQueryResult 进行返回
        return handleQueryResult(queryResult,clazz);
    }


}
