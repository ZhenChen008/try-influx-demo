package com.chen.example.service;

import com.chen.example.entity.Firsttext;

import java.util.List;

public interface FirsttextService {

    //方法-1  保存
    void save(Firsttext forecast);

    //方法-2  查询全部
    List<Firsttext> findAll();

}
