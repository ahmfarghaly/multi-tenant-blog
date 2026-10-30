package dev.ams.regionalblog.gateway.dto;

import dev.ams.regionalblog.gateway.domain.TenantPlacement;

public record TenantPlacementDto(
    String tenantId,
    String region,
    String shardId,
    String status
) {
    public static TenantPlacementDto fromEntity(TenantPlacement entity) {
        return new TenantPlacementDto(
            entity.getTenantId(),
            entity.getRegion(),
            entity.getShardId(),
            entity.getStatus()
        );
    }
}
