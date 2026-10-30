package dev.ams.regionalblog.blog.config;

import dev.ams.regionalblog.blog.tenant.TenantContext;
import dev.ams.regionalblog.blog.tenant.TenantRequestContext;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HibernateTenantIdentifierResolver
        implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

    public static final String UNRESOLVED_TENANT = "NO_TENANT_CONTEXT";

    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.get()
                .map(TenantRequestContext::tenantId)
                .orElse(UNRESOLVED_TENANT);
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}
