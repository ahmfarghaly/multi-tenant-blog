package dev.ams.regionalblog.blog.routing;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceRoutingConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceRoutingConfig.class);

    private final ShardRoutingProperties properties;

    public DataSourceRoutingConfig(ShardRoutingProperties properties) {
        this.properties = properties;
    }

    @Bean(name = "shard1DataSource", destroyMethod = "close")
    public HikariDataSource shard1DataSource() {
        return createPool("shard-1", properties.shard1());
    }

    @Bean(name = "shard2DataSource", destroyMethod = "close")
    public HikariDataSource shard2DataSource() {
        return createPool("shard-2", properties.shard2());
    }

    private HikariDataSource createPool(String shardName, ShardRoutingProperties.ShardPoolConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("HikariPool-" + properties.regionId() + "-" + shardName);
        hikariConfig.setJdbcUrl(config.jdbcUrl());
        hikariConfig.setUsername(config.username());
        hikariConfig.setPassword(config.password());
        hikariConfig.setMaximumPoolSize(config.maximumPoolSize() > 0 ? config.maximumPoolSize() : 5);
        hikariConfig.setMinimumIdle(config.minimumIdle() > 0 ? config.minimumIdle() : 2);
        hikariConfig.setConnectionTimeout(5000);
        hikariConfig.setValidationTimeout(3000);
        return new HikariDataSource(hikariConfig);
    }

    public static class ShardFlywayMigrator {
    }

    @Bean
    public ShardFlywayMigrator shardFlywayMigrator(
            @Qualifier("shard1DataSource") DataSource shard1,
            @Qualifier("shard2DataSource") DataSource shard2) {

        log.info("Executing Flyway migrations across all configured shards in region '{}'", properties.regionId());
        runFlywayMigration("shard-1", shard1);
        runFlywayMigration("shard-2", shard2);
        return new ShardFlywayMigrator();
    }

    @Bean
    @DependsOn("shardFlywayMigrator")
    public ShardRoutingDataSource shardRoutingDataSource(
            @Qualifier("shard1DataSource") DataSource shard1,
            @Qualifier("shard2DataSource") DataSource shard2) {

        ShardRoutingDataSource routingDataSource = new ShardRoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("shard-1", shard1);
        targetDataSources.put("shard-2", shard2);

        routingDataSource.setTargetDataSources(targetDataSources);
        // Fail closed: do not fall back to any default database for unknown or missing shards
        routingDataSource.setLenientFallback(false);
        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }

    @Bean
    @Primary
    @DependsOn("shardFlywayMigrator")
    public DataSource dataSource(ShardRoutingDataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    private void runFlywayMigration(String shardId, DataSource dataSource) {
        try {
            log.info("Executing Flyway migration for shard '{}' in region '{}'...", shardId, properties.regionId());
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .load();
            flyway.migrate();
            log.info("Flyway migration completed successfully for shard '{}'", shardId);
        } catch (Exception e) {
            log.error("Flyway migration failed for shard '{}': {}", shardId, e.getMessage(), e);
            throw new IllegalStateException("Failed to migrate database shard: " + shardId, e);
        }
    }
}
