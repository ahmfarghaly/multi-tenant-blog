package dev.ams.regionalblog.blog.routing;

import dev.ams.regionalblog.blog.tenant.TenantContext;
import dev.ams.regionalblog.blog.tenant.TenantRequestContext;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class ShardRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.get()
                .map(TenantRequestContext::shardId)
                .orElse(null);
    }
}
