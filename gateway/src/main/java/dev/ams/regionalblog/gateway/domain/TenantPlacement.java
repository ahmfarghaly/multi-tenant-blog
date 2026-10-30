package dev.ams.regionalblog.gateway.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "tenant_placement")
public class TenantPlacement {

    @Id
    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(name = "region", nullable = false, length = 30)
    private String region;

    @Column(name = "shard_id", nullable = false, length = 30)
    private String shardId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public TenantPlacement() {
    }

    public TenantPlacement(String tenantId, String region, String shardId, String status) {
        this.tenantId = tenantId;
        this.region = region;
        this.shardId = shardId;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getShardId() {
        return shardId;
    }

    public void setShardId(String shardId) {
        this.shardId = shardId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
