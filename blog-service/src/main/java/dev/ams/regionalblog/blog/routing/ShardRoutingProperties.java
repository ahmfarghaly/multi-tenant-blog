package dev.ams.regionalblog.blog.routing;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.regional")
public record ShardRoutingProperties(
    String regionId,
    String directoryBaseUrl,
    int cacheTtlSeconds,
    ShardPoolConfig shard1,
    ShardPoolConfig shard2
) {
    public record ShardPoolConfig(
        String jdbcUrl,
        String username,
        String password,
        int maximumPoolSize,
        int minimumIdle
    ) {}

    public ShardRoutingProperties {
        if (regionId == null || regionId.isBlank()) {
            regionId = "eu";
        }
        if (directoryBaseUrl == null || directoryBaseUrl.isBlank()) {
            directoryBaseUrl = "http://localhost:8080";
        }
        if (cacheTtlSeconds <= 0) {
            cacheTtlSeconds = 30;
        }
    }
}
