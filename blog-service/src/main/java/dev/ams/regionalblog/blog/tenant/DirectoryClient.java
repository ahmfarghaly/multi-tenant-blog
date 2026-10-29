package dev.ams.regionalblog.blog.tenant;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ams.regionalblog.blog.routing.ShardRoutingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;

import java.time.Duration;
import java.util.Optional;

@Component
public class DirectoryClient {

    private static final Logger log = LoggerFactory.getLogger(DirectoryClient.class);

    public record PlacementRecord(
        String tenantId,
        String region,
        String shardId,
        String status
    ) {}

    private final RestClient restClient;
    private final ShardRoutingProperties properties;
    private final Cache<String, Optional<PlacementRecord>> cache;

    public DirectoryClient(ShardRoutingProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
        this.cache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofSeconds(Math.max(1, properties.cacheTtlSeconds())))
                .build();
    }

    public Optional<PlacementRecord> getPlacement(String tenantId) {
        return cache.get(tenantId, this::fetchPlacement);
    }

    public void invalidateCache(String tenantId) {
        if (tenantId != null) {
            cache.invalidate(tenantId);
        } else {
            cache.invalidateAll();
        }
    }

    private Optional<PlacementRecord> fetchPlacement(String tenantId) {
        String url = properties.directoryBaseUrl() + "/internal/placements/" + tenantId;
        log.debug("Fetching placement for tenant {} from {}", tenantId, url);

        try {
            PlacementRecord placement = restClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                        // Handled below via HttpClientErrorException.NotFound
                    })
                    .body(PlacementRecord.class);

            return Optional.ofNullable(placement);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Tenant {} not found in directory", tenantId);
            return Optional.empty();
        } catch (ResourceAccessException e) {
            log.error("Failed to connect to tenant directory at {}: {}", url, e.getMessage());
            throw new DirectoryUnavailableException("Tenant directory service unavailable", e);
        } catch (Exception e) {
            log.error("Error retrieving tenant placement for {}: {}", tenantId, e.getMessage(), e);
            throw new DirectoryUnavailableException("Failed to resolve tenant placement", e);
        }
    }
}
