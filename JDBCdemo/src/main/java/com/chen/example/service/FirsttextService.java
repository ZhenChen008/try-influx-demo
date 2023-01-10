package com.chen.example.service;

import com.chen.example.entity.Firsttext;
import com.chen.example.entity.TestPoint;
import org.influxdb.dto.BatchPoints;

import java.util.List;

public interface FirsttextService {

    //方法-1  保存一条数据
    void save(Firsttext forecast);

    //方法-2  查询全部
    List<Firsttext> findAll();

    // 方法-3 插入数据（使用 POJO的方式）
    void insertByPojo(TestPoint point);

    // 方法-4 查询全部并且使用 Mapper
    List<TestPoint> selectAllByMapper();

    // 方法-5    批量写入数据 -1
    void  insertByBatch(BatchPoints batchPoints);

    // 方法-6    批量写入数据 -2
    void  batchLinesInsert( List<String> records);

}
