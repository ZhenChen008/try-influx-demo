package com.chen.example.config;

import com.chen.example.entity.TestPoint;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**     工具类 Util是  utiliy
 *  本来是通用的、与业务无关的，可以独立出来，可供其他项目使用。
 *
 *  本文件 类似 工具类，但也有 Dao功能
 *  Dao的作用是封装对数据库的访问：增删改查，不涉及业务逻辑
 *
 */
/*             这里有的使用 @Component，然后使用时就是   @Configuration 和 @Component 到底有啥区别？
             @Autowired
    private InfluxDBUtils influxDBUtils;*/
@Configuration
public class InfluxDBTemplate {


    //1.influxDBTemplate ---> influxdb---> 数据库 获取 influxdb 连接


    private final InfluxdbProperties influxdbProperties;

    private InfluxDB influxDB;

    @Autowired
    public InfluxDBTemplate(InfluxdbProperties influxdbProperties) {
        this.influxdbProperties = influxdbProperties;
        getInfluxDB(); // 一开始 就获取了连接
    }

    /**
     * 方法 1-获取 influxdb 连接
     */
    public void getInfluxDB() {
        if (influxDB == null) {
            influxDB = InfluxDBFactory.connect(
                                                influxdbProperties.getUrl(),
                                                influxdbProperties.getUsername(),
                                                influxdbProperties.getPassword() );
            //设置使用数据库  保证库存在
            influxDB.setDatabase(influxdbProperties.getDatabase());
            //设置数据库保留策略 保证策略存在,yml中的作为默认 保留策略
            if (ObjectUtils.isEmpty(influxdbProperties.getRetention())) {
                influxDB.setRetentionPolicy(influxdbProperties.getRetention());
            }
        }
        influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);  // 设置日志打印级别  NONE、 BASIC、HEADERS 打印更加详细
    }


    /**
     * 方法 2-关闭 数据库连接
     */
    public void close() {
        if (influxDB != null) {
            influxDB.close();
        }
    }


/*      从JDBC测试复制 过来和插入代码参考一下吧！ java2.23版本
    {
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
    */


    /**
     * 方法 3-原始的插入数据     插入单条数据原始的写法,方法重载 1/3
     *
     * @param measurement
     *       插入的表的名称
     */
    public void write(String measurement)
    {
        //首先创建   Point point , 指定时间精确到 微秒 TimeUnit.MICROSECONDS
        Point buildPoint = Point.measurement("test")
                .tag("tags-1", "green")
                .tag("tag-2", "Site04")
                .addField("wind风力", 3.0)
                .addField("water水量", 123L)
//                .time(new Date().getTime(), TimeUnit.MICROSECONDS).build();
                .addField("备注","不指定time").build();

        //单条插入,需要先定义 Point    influxDB.write(Point point)
        influxDB.write(buildPoint);
        close();
    }


    /**
     * 方法 4-指定时间插入     插入单条数据的 HashMap写法,方法重载 2/3
     *
     * @param measurement 表
     * @param tags        标签 Map
     * @param fields      字段 Map
     * @param time        输入指定的时间 Long类型
     * @param timeUnit    时间单位
     */
    public void write(String measurement, Map<String, String> tags, Map<String, Object> fields, long time, TimeUnit timeUnit) {
        Point point = Point.measurement(measurement)   // 按照 Map赋值 tag和 fields
                .tag(tags)
                .fields(fields)
                .time(time, timeUnit)
                .build();
        System.out.println("Let us look at the Point: \n"+point);
        influxDB.write(point);
        close();
    }


    /**
     * 方法 5-插入数据-自动生成时间     调用单条数据的 HashMap写法,方法重载 3/3
     *
     * @param measurement 表
     * @param tags        标签 Map
     * @param fields      字段 Map
     */
    public void write(String measurement, Map<String, String> tags, Map<String, Object> fields) {
        write(measurement, tags, fields, System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }


    /**
     * 方法 6-插入单条数据
     * influxDB开启UDP功能, 默认端口:8089,默认数据库:udp,没提供代码传数据库功能接口
     * 使用 UDP的原因
     * TCP数据传输慢，UDP数据传输快。
     * 网络带宽需求较小，而实时性要求高。
     * InfluxDB和服务器在同机房，发生数据丢包的可能性较小，即使真的发生丢包，对整个请求流量的收集影响也较小。
     *
     *
     * @param measurement  表名称
     * @param tags         标签 Map
     * @param fields       字段 Map
     */
    public void writeByUdp(String measurement, Map<String, String> tags, Map<String, Object> fields) {
        //构建
        Point.Builder builder = Point.measurement(measurement);
        //可指定时间戳
        builder.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        //tag属性只能存储String类型
        builder.tag(tags);
        //设置field
        builder.fields(fields);
        int udpPort = 8089;
        influxDB.write(udpPort, builder.build());
    }


    /**
     * 方法 7-批量插入
     * 时间序列数据往往由许多小点组成，一次写入这些记录将非常低效。首选方法是分批收集记录。
     * InfluxDB API 提供了一个BatchPoint对象.
     *
     * 请注意，我们必须将 BatchPoint 与数据库和保留策略相关联。
     * 批量发送消息有两种模式：定时定量（这个词等会具体解释）和 BatchPoints。
     * 参考： https://blog.csdn.net/qq_35981283/article/details/79766231
     * 也可以看看 https://www.saoniuhuo.com/article/detail-216070.html
     *
     *
     * @param batchPoints 批量记录  推荐 1000 条作为一个批
     */
    public void writeBatch(BatchPoints batchPoints ) {
        influxDB.write(batchPoints);
        close();
    }



    /**
     * 方法 8-用来执行查询操作，返回原始的查询结果 QueryResult (待解析处理)
     *
     * @param command 执行命令
     * @return 返回结果
     */
    public QueryResult query(String command,String database) {
        QueryResult queryResult = influxDB.query(new Query(command, database));

        close();
        return queryResult;
    }


    /**
     * 方法 9- select 查询封装处理
     * handleQueryResult(QueryResult res, Class<T> clazz )
     *
     * @param queryResult  查询返回结果
     * @param clazz 封装的对象类型
     * @param <T>   泛型
     * @return   返回处理结果
     *
     */

    //参考 其他人是  //把查询出的结果集转换成对应的实体对象，聚合成list
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
     * 方法 10- 进一步封装 select查询功能
     * @param selcetCommand  select查询语句
     * @param clazz             类型
     * @param <T>             泛   型
     * @return                  结果
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


    /**
     * 插入   网上复制 的
     *
     * @param measurement
     *            表
     * @param tags
     *            标签
     * @param fields
     *            字段

    public void insert(String measurement, Map<String, String> tags,
                       Map<String, Object> fields) {
        Builder builder = Point.measurement(measurement);
        builder.tag(tags);
        builder.fields(fields);

        influxDB.write(database, "", builder.build());
    }

     */
/*        3.4. 设置日志记录级别
    在内部，
    InfluxDB API 使用 Retrofit 并通过日志拦截器向 Retrofit 的日志记录工具公开一个接口。

    因此，我们可以使用以下方法设置日志记录级别：

            influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);

        influxDB = InfluxDBFactory.connect(databaseUrl, username, password);
        influxDB.enableBatch(2000, 1000, TimeUnit.MILLISECONDS);
        influxDB.setLogLevel(LogLevel.NONE);

*/

    // 查看方法之间的 调用关系： https://cloud.tencent.com/developer/article/1830952


    /**   后面补充的 MVC 形式插入数据点 Point
     *
     * @param build_point 插入的对象经过 build()之后的 Point
     */
    public void write( Point build_point)
    {
        influxDB.write("loudi","rp_30_days",build_point);
        //单条插入,需要先定义 Point    influxDB.write(Point point)
        close();
    }


}
