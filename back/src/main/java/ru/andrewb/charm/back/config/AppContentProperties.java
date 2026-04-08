package ru.andrewb.charm.back.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.content")
public class AppContentProperties {

    private String basePath;
}
