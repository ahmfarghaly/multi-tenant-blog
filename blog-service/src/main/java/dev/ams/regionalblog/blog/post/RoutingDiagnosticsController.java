package dev.ams.regionalblog.blog.post;

import dev.ams.regionalblog.blog.routing.ShardRoutingProperties;
import dev.ams.regionalblog.blog.tenant.TenantContext;
import dev.ams.regionalblog.blog.tenant.TenantRequestContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/routing-info")
@ConditionalOnProperty(name = "app.diagnostics.enabled", havingValue = "true", matchIfMissing = true)
public class RoutingDiagnosticsController {

    private final ShardRoutingProperties properties;

    public RoutingDiagnosticsController(ShardRoutingProperties properties) {
        this.properties = properties;
    }

    public record RoutingDiagnosticsResponse(
        String tenantId,
        String region,
        String shardId,
        String databaseIdentifier
    ) {}

    @GetMapping
    public RoutingDiagnosticsResponse getRoutingInfo() {
        TenantRequestContext context = TenantContext.require();
        String dbIdentifier = properties.regionId() + "-" + context.shardId() + "/blog";

        return new RoutingDiagnosticsResponse(
            context.tenantId(),
            context.region(),
            context.shardId(),
            dbIdentifier
        );
    }
}
