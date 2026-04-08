package ru.andrewb.charm.back.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.redis")
public class AppRedisProperties {

    private String host;
    private Integer port;
    private Integer timeoutMs;

    private Integer charmQueueTtlSec;
    private Integer charmEmptyTtlSec;
    private Integer charmLockTtlSec;

    private Pool pool =  new Pool();

    @Getter
    @Setter
    public static class Pool {
        private Integer maxTotal;
        private Integer maxIdle;
        private Integer minIdle;
        private Boolean testOnBorrow;
    }
}
