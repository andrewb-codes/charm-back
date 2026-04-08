package ru.andrewb.charm.back.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.datasource")
public class AppDataSourceProperties {

    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String poolImpl;
    private Integer poolSize;

    private Integer fetchSize;
    private Integer maxRows;
    private Integer queryTimeout;
}
