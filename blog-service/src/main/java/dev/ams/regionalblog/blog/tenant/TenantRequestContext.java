package dev.ams.regionalblog.blog.tenant;

public record TenantRequestContext(
    String tenantId,
    String region,
    String shardId
) {
    public TenantRequestContext {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId cannot be blank");
        }
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("region cannot be blank");
        }
        if (shardId == null || shardId.isBlank()) {
            throw new IllegalArgumentException("shardId cannot be blank");
        }
    }
}
