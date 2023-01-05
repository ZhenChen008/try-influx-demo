package com.chen.example.entity;


import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**    用来映射 数据库表的 数据
 *       up的是 forcast -----对应表的对象 名称
 *      我这里随便取为 Firsttext
 *
 */

@Data
public class Firsttext {
    // 时间项 可以固定 ,暂时确定为 Long 类型
    private Long time;

    //处理其它两项———— tags、fields
    private Map<String,String> tags = new HashMap<>();  //这里是根据实战一 后面添加 为了方便写的
    private Map<String,Object> fields= new HashMap<>();

    /**
     * 然后加上 Getter、Setter() 方法 toString()
     *      使用 lombok 的 @Data
     */
}
