package dev.ams.cloud.multitenant.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Component;

import dev.ams.cloud.multitenant.dao.config.DataSourceRepository;
import dev.ams.cloud.multitenant.model.config.DataSourceConfig;

@Component
public class TenantDataSource implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5118444271147813749L;

	private ConcurrentHashMap<String, DataSource> dataSources = new ConcurrentHashMap<>();
	
	@Autowired
	private DataSourceRepository dataSourceRepository;
	
	synchronized public DataSource getDataSource(String name) {
		if (dataSources.get(name) != null) {
            return dataSources.get(name);
        }
        DataSource dataSource = createDataSource(name);
        if (dataSource != null) {
            dataSources.put(name, dataSource);
        }
        return dataSource;
	}
	
	synchronized private DataSource createDataSource(String name) {
        DataSourceConfig config = dataSourceRepository.findByName(name);
        if (config != null) {
            DataSourceBuilder factory = DataSourceBuilder
                    .create().driverClassName(config.getDriverClassName())
                    .username(config.getUsername())
                    .password(config.getPassword())
                    .url(config.getUrl());
            DataSource ds = factory.build();     
            return ds;
        }
        return null;
    }
	
	@PostConstruct
    public Map<String, DataSource> getAll() {
        List<DataSourceConfig> configList = dataSourceRepository.findAll();
        Map<String, DataSource> result = new HashMap();
        for (DataSourceConfig config : configList) {
            DataSource dataSource = getDataSource(config.getName());
            result.put(config.getName(), dataSource);
        }
        return result;
    }
}
