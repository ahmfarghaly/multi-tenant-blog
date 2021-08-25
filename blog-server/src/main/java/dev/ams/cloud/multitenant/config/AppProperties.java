package dev.ams.cloud.multitenant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties("app.props")
public class AppProperties {

	private String defaultTenant;
}
