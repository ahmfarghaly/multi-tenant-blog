package dev.ams.cloud.multitenant.dao.config;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.ams.cloud.multitenant.model.config.DataSourceConfig;
import dev.ams.cloud.multitenant.model.config.TenantDTO;

public interface DataSourceRepository extends JpaRepository<DataSourceConfig, Long> {

	DataSourceConfig findByName(String name);
	
	List<TenantDTO> findAllProjectedBy();
}
