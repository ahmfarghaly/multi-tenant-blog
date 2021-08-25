package dev.ams.cloud.multitenant.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cfg.Environment;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
public class HibernateMultitenantConfig {

	@Autowired
	org.springframework.core.env.Environment env;
	
	@Autowired
	DataSource dataSource;
	
	@Autowired
    private JpaProperties jpaProperties;
	
	@Bean("entityManagerFactory")
	LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(DataSource ds, MultiTenantConnectionProvider connectionProvider, CurrentTenantIdentifierResolver identifierResolver) {
		
		Map<String,Object> hibernateProperties = new HashMap<String, Object>(jpaProperties.getProperties()); 
		hibernateProperties.put(Environment.MULTI_TENANT, MultiTenancyStrategy.SCHEMA);
		hibernateProperties.put(Environment.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
		hibernateProperties.put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, identifierResolver);
		hibernateProperties.put(Environment.HBM2DDL_DATABASE_ACTION, jpaProperties.getProperties().get("ddl-auto"));
		
		
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setJpaPropertyMap(hibernateProperties);
		em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		em.setPackagesToScan(jpaProperties.getProperties().get("packagesToScan"));
		return em;
	}

}
