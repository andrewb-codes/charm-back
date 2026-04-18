package ru.andrewb.charm.back.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class AppJwtProperties {

    private String secret;
    private Long accessTokenTtlMin;
}
