package dev.ams.regionalblog.gateway.routing;

import dev.ams.regionalblog.gateway.config.GatewayProperties;
import dev.ams.regionalblog.gateway.domain.TenantPlacement;
import dev.ams.regionalblog.gateway.domain.TenantPlacementRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.UUID;

@Service
public class RegionalProxyService {

    private static final Logger log = LoggerFactory.getLogger(RegionalProxyService.class);

    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
    public static final String HEADER_LEGACY_TENANT_ID = "X-TenantID";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";

    private final TenantPlacementRepository placementRepository;
    private final GatewayProperties gatewayProperties;
    private final RestClient restClient;

    public RegionalProxyService(
            TenantPlacementRepository placementRepository,
            GatewayProperties gatewayProperties,
            RestClient.Builder restClientBuilder) {
        this.placementRepository = placementRepository;
        this.gatewayProperties = gatewayProperties;
        this.restClient = restClientBuilder.build();
    }

    public ResponseEntity<byte[]> forwardRequest(HttpServletRequest request) {
        String correlationId = extractOrGenerateCorrelationId(request);
        MDC.put("correlationId", correlationId);

        try {
            String tenantId = extractTenantId(request);
            if (tenantId == null || tenantId.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required X-Tenant-Id header");
            }

            TenantPlacement placement = placementRepository.findById(tenantId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown tenant: " + tenantId));

            if (!"ACTIVE".equalsIgnoreCase(placement.getStatus())) {
                log.warn("Access attempt for inactive tenant: {} status: {}", tenantId, placement.getStatus());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant is not active: " + placement.getStatus());
            }

            String regionalBaseUrl = gatewayProperties.getBaseUrlForRegion(placement.getRegion());
            if (regionalBaseUrl == null || regionalBaseUrl.isBlank()) {
                log.error("No configured regional application for region: {}", placement.getRegion());
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No regional deployment configured for region: " + placement.getRegion());
            }

            String requestUri = request.getRequestURI();
            String queryString = request.getQueryString();
            String targetUrl = regionalBaseUrl + requestUri + (queryString != null && !queryString.isBlank() ? "?" + queryString : "");

            log.info("Routing request for tenant: {} (region: {}, shard: {}) -> {}", tenantId, placement.getRegion(), placement.getShardId(), targetUrl);

            return executeForward(request, targetUrl, tenantId, correlationId);
        } finally {
            MDC.remove("correlationId");
        }
    }

    private ResponseEntity<byte[]> executeForward(
            HttpServletRequest request,
            String targetUrl,
            String tenantId,
            String correlationId) {

        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        byte[] body = readRequestBody(request);

        try {
            RestClient.RequestBodySpec requestSpec = restClient
                    .method(method)
                    .uri(URI.create(targetUrl))
                    .header(HEADER_TENANT_ID, tenantId)
                    .header(HEADER_CORRELATION_ID, correlationId);

            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    if (!name.equalsIgnoreCase(HttpHeaders.HOST)
                            && !name.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)
                            && !name.equalsIgnoreCase(HEADER_TENANT_ID)
                            && !name.equalsIgnoreCase(HEADER_LEGACY_TENANT_ID)
                            && !name.equalsIgnoreCase(HEADER_CORRELATION_ID)) {
                        for (String val : Collections.list(request.getHeaders(name))) {
                            requestSpec.header(name, val);
                        }
                    }
                }
            }

            if (body != null && body.length > 0) {
                String contentType = request.getContentType();
                if (contentType != null && !contentType.isBlank()) {
                    requestSpec.contentType(MediaType.parseMediaType(contentType));
                }
                requestSpec.body(new ByteArrayResource(body));
            }

            return requestSpec.exchange((clientRequest, clientResponse) -> {
                byte[] responseBytes = clientResponse.getBody().readAllBytes();
                HttpHeaders responseHeaders = new HttpHeaders();
                clientResponse.getHeaders().forEach((name, values) -> {
                    if (!name.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING)) {
                        responseHeaders.put(name, values);
                    }
                });
                responseHeaders.set(HEADER_CORRELATION_ID, correlationId);
                return new ResponseEntity<>(responseBytes, responseHeaders, clientResponse.getStatusCode());
            });

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpHeaders errorHeaders = new HttpHeaders();
            errorHeaders.set(HEADER_CORRELATION_ID, correlationId);
            return new ResponseEntity<>(e.getResponseBodyAsByteArray(), errorHeaders, e.getStatusCode());
        } catch (ResourceAccessException e) {
            log.error("Failed to connect to regional app at {}: {}", targetUrl, e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Regional application unavailable", e);
        } catch (Exception e) {
            log.error("Unexpected error proxying to {}: {}", targetUrl, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Proxy routing failed", e);
        }
    }

    private String extractTenantId(HttpServletRequest request) {
        String tenantId = request.getHeader(HEADER_TENANT_ID);
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = request.getHeader(HEADER_LEGACY_TENANT_ID);
        }
        return tenantId;
    }

    private String extractOrGenerateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(HEADER_CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }

    private byte[] readRequestBody(HttpServletRequest request) {
        try (InputStream in = request.getInputStream()) {
            return in.readAllBytes();
        } catch (IOException e) {
            log.warn("Failed to read incoming request body: {}", e.getMessage());
            return new byte[0];
        }
    }
}
