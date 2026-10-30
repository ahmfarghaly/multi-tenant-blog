package dev.ams.regionalblog.blog;

import dev.ams.regionalblog.blog.routing.ShardRoutingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = {FlywayAutoConfiguration.class})
@EnableConfigurationProperties(ShardRoutingProperties.class)
public class BlogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogServiceApplication.class, args);
    }
}
