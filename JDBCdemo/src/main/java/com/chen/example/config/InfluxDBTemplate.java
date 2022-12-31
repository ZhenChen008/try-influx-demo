package com.chen.example.config;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

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




}
