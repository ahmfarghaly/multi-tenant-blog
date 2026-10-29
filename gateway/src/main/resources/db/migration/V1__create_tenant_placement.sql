CREATE TABLE tenant_placement (
    tenant_id  VARCHAR(100) PRIMARY KEY,
    region     VARCHAR(30) NOT NULL,
    shard_id   VARCHAR(30) NOT NULL,
    status     VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed core demo tenants per specification Section 4
INSERT INTO tenant_placement (tenant_id, region, shard_id, status) VALUES
    ('tenant-eu-001',   'eu',   'shard-1', 'ACTIVE'),
    ('tenant-eu-002',   'eu',   'shard-2', 'ACTIVE'),
    ('tenant-me-001',   'me',   'shard-1', 'ACTIVE'),
    ('tenant-me-002',   'me',   'shard-2', 'ACTIVE'),
    ('tenant-apac-001', 'apac', 'shard-1', 'ACTIVE'),
    ('tenant-apac-002', 'apac', 'shard-2', 'ACTIVE');
