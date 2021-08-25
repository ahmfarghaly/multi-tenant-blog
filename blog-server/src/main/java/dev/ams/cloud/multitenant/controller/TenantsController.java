package dev.ams.cloud.multitenant.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.ams.cloud.multitenant.dao.config.DataSourceRepository;
import dev.ams.cloud.multitenant.model.config.TenantDTO;

@RestController
@RequestMapping(value="/tenants",produces = MediaType.APPLICATION_JSON_VALUE)
public class TenantsController {

	@Autowired
	private DataSourceRepository repository;
	
	@GetMapping
	public List<TenantDTO> getAll() {
		return repository.findAllProjectedBy();
	}
	
	
}
