package dev.ams.cloud.multitenant.config;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TenantResolver implements CurrentTenantIdentifierResolver {

	@Autowired
	AppProperties appProperties;
	
	@Override
	public String resolveCurrentTenantIdentifier() {
		String tenant = TenantContext.getCurrentTenant();
		if(tenant!=null) {
			return tenant;
		} else {
			return appProperties.getDefaultTenant();
		}
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return true;
	}

}
