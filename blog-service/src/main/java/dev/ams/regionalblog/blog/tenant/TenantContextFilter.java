package dev.ams.regionalblog.blog.tenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ams.regionalblog.blog.routing.ShardRoutingProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class TenantContextFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantContextFilter.class);

    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
    public static final String HEADER_LEGACY_TENANT_ID = "X-TenantID";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";

    private static final Set<String> ALLOWED_SHARDS = Set.of("shard-1", "shard-2");

    private final DirectoryClient directoryClient;
    private final ShardRoutingProperties properties;
    private final ObjectMapper objectMapper;

    public TenantContextFilter(
            DirectoryClient directoryClient,
            ShardRoutingProperties properties,
            ObjectMapper objectMapper) {
        this.directoryClient = directoryClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Health and actuator endpoints bypass tenant validation
        return path.startsWith("/actuator") || path.equals("/error");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String correlationId = request.getHeader(HEADER_CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put("correlationId", correlationId);

        try {
            String tenantId = request.getHeader(HEADER_TENANT_ID);
            if (tenantId == null || tenantId.isBlank()) {
                tenantId = request.getHeader(HEADER_LEGACY_TENANT_ID);
            }

            if (tenantId == null || tenantId.isBlank()) {
                writeError(response, HttpStatus.BAD_REQUEST, "Missing required X-Tenant-Id header");
                return;
            }

            Optional<DirectoryClient.PlacementRecord> placementOpt;
            try {
                placementOpt = directoryClient.getPlacement(tenantId);
            } catch (DirectoryUnavailableException e) {
                log.error("Directory service unreachable during placement lookup for tenant {}", tenantId, e);
                writeError(response, HttpStatus.SERVICE_UNAVAILABLE, "Directory service unavailable");
                return;
            }

            if (placementOpt.isEmpty()) {
                writeError(response, HttpStatus.NOT_FOUND, "Unknown tenant: " + tenantId);
                return;
            }

            DirectoryClient.PlacementRecord placement = placementOpt.get();

            // 1. Verify tenant status
            if (!"ACTIVE".equalsIgnoreCase(placement.status())) {
                log.warn("Access rejected for inactive tenant: {} status: {}", tenantId, placement.status());
                writeError(response, HttpStatus.FORBIDDEN, "Tenant is not active: " + placement.status());
                return;
            }

            // 2. Regional Ownership Check: must fail closed if tenant region doesn't match this deployment
            if (!placement.region().equalsIgnoreCase(properties.regionId())) {
                log.warn("Cross-region access rejected! Tenant: {} assigned region: {}, but current regional deployment: {}",
                        tenantId, placement.region(), properties.regionId());
                writeError(response, HttpStatus.FORBIDDEN,
                        "Cross-region request rejected: tenant belongs to region '" + placement.region()
                                + "', but regional deployment is '" + properties.regionId() + "'");
                return;
            }

            // 3. Shard validation
            if (!ALLOWED_SHARDS.contains(placement.shardId().toLowerCase())) {
                log.error("Unknown shard ID '{}' assigned to tenant '{}'", placement.shardId(), tenantId);
                writeError(response, HttpStatus.INTERNAL_SERVER_ERROR, "Unknown shard ID: " + placement.shardId());
                return;
            }

            // 4. Bind immutable request context
            TenantRequestContext context = new TenantRequestContext(tenantId, placement.region(), placement.shardId());
            TenantContext.set(context);

            MDC.put("tenantId", tenantId);
            MDC.put("region", placement.region());
            MDC.put("shardId", placement.shardId());

            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
            MDC.clear();
        }
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> errorBody = Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        );
        objectMapper.writeValue(response.getOutputStream(), errorBody);
    }
}
