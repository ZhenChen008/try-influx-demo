package com.chen.example.entity;


import java.util.HashMap;
import java.util.Map;

/**    用来映射 数据库表的 数据
 *       up的是 forcast -----对应表的对象 名称
 *      我这里随便取为 Firsttext
 *
 */
public class Firsttext {
    // 时间项 可以固定
    private Long time;

    //处理其它两项———— tags、fields
    private Map<String,String> tags = new HashMap<>();  //这里是根据实战一 后面添加 为了方便写的
    private Map<String,Object> fields= new HashMap<>();

    /**  然后加上 Getter、Setter() 方法
     */
    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "Firsttext{" +
                "time=" + time +
                ", tags=" + tags +
                ", fields=" + fields +
                '}';
    }
}
