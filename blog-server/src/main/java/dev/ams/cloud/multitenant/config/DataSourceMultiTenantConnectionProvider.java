package dev.ams.cloud.multitenant.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.DataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

//@Component
public class DataSourceMultiTenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = 619970038417240710L;
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	AppProperties appProperties;
	
	@Autowired
	private DataSource defaultDS;

	private Map<String, DataSource> dsMap = new HashMap<String, DataSource>();
	
	boolean init = false;
	
	@PostConstruct
	public void load() {
		dsMap.put(appProperties.getDefaultTenant(), defaultDS);
	}
	
	@Override
	protected DataSource selectAnyDataSource() {
		
		return dsMap.get(appProperties.getDefaultTenant());
	}

	@Override
	protected DataSource selectDataSource(String tenantIdentifier) {

		if(!init) {
			init = true;
			TenantDataSource tenantDataSource = context.getBean(TenantDataSource.class);
			dsMap.putAll(tenantDataSource.getAll());
		}
		return dsMap.get(tenantIdentifier)!=null?dsMap.get(tenantIdentifier):defaultDS;
	}

}
