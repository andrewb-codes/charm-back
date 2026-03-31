package ru.andrewb.charm.pool.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomDataSourceConfig {
    private String jdbcUrl;
    private String username;
    private String password;
    private int maximumPoolSize;
}