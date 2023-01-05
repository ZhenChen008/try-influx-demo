package com.chen.example.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("influxdbconninfo")
public class InfluxdbProperties {
    private String url;
    private String username;
    private String password;
    private String database;
    private String retention;//保留策略


    //然后添加 Getter()/Setter() 方法,使用lombok的 @Data
}
