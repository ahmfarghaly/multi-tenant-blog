package dev.ams.regionalblog.gateway.dto;

import jakarta.validation.constraints.NotBlank;

public record UpsertPlacementRequest(
    @NotBlank(message = "region is required")
    String region,

    @NotBlank(message = "shardId is required")
    String shardId,

    @NotBlank(message = "status is required")
    String status
) {}
