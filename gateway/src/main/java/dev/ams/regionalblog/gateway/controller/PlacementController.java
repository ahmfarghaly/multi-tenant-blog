package dev.ams.regionalblog.gateway.controller;

import dev.ams.regionalblog.gateway.domain.TenantPlacement;
import dev.ams.regionalblog.gateway.domain.TenantPlacementRepository;
import dev.ams.regionalblog.gateway.dto.TenantPlacementDto;
import dev.ams.regionalblog.gateway.dto.UpsertPlacementRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class PlacementController {

    private final TenantPlacementRepository repository;

    public PlacementController(TenantPlacementRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/internal/placements/{tenantId}")
    public TenantPlacementDto getPlacement(@PathVariable String tenantId) {
        return repository.findById(tenantId)
                .map(TenantPlacementDto::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown tenant: " + tenantId));
    }

    @GetMapping({"/internal/placements", "/api/tenants"})
    public List<TenantPlacementDto> listPlacements() {
        return repository.findAllByOrderByTenantIdAsc().stream()
                .map(TenantPlacementDto::fromEntity)
                .toList();
    }

    @PutMapping("/internal/placements/{tenantId}")
    public ResponseEntity<TenantPlacementDto> upsertPlacement(
            @PathVariable String tenantId,
            @Valid @RequestBody UpsertPlacementRequest request) {

        TenantPlacement placement = repository.findById(tenantId)
                .orElseGet(() -> new TenantPlacement(tenantId, request.region(), request.shardId(), request.status()));

        placement.setRegion(request.region());
        placement.setShardId(request.shardId());
        placement.setStatus(request.status());

        TenantPlacement saved = repository.save(placement);
        return ResponseEntity.ok(TenantPlacementDto.fromEntity(saved));
    }
}
