package com.chen.example.service;

import com.chen.example.entity.Firsttext;
import com.chen.example.entity.TestPoint;

import java.util.List;

public interface FirsttextService {

    //方法-1  保存一条数据
    void save(Firsttext forecast);

    //方法-2  查询全部
    List<Firsttext> findAll();

    // 方法-3 插入数据（使用 POJO的方式）
    void insertByPojo(TestPoint point);

    // 方法-4 查询全部并且使用Mapper
    List<TestPoint> selectAllByMapper();

}
