package dev.ams.regionalblog.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.regional")
public record GatewayProperties(
    String euUrl,
    String meUrl,
    String apacUrl
) {
    public String getBaseUrlForRegion(String region) {
        if (region == null) {
            return null;
        }
        return switch (region.toLowerCase().trim()) {
            case "eu" -> euUrl;
            case "me" -> meUrl;
            case "apac" -> apacUrl;
            default -> null;
        };
    }
}
