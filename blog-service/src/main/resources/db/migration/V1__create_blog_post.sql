CREATE TABLE blog_post (
    id         UUID PRIMARY KEY,
    tenant_id  VARCHAR(100) NOT NULL,
    title      VARCHAR(200) NOT NULL,
    body       TEXT NOT NULL,
    status     VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_blog_post_tenant_created
    ON blog_post (tenant_id, created_at DESC);

CREATE INDEX idx_blog_post_tenant_status
    ON blog_post (tenant_id, status);
