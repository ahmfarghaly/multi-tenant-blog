package dev.ams.regionalblog.blog.tenant;

import java.util.Objects;
import java.util.Optional;

public final class TenantContext {

    private static final ThreadLocal<TenantRequestContext> CURRENT_CONTEXT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(TenantRequestContext context) {
        Objects.requireNonNull(context, "TenantRequestContext cannot be null");
        TenantRequestContext existing = CURRENT_CONTEXT.get();
        if (existing != null && !existing.equals(context)) {
            throw new IllegalStateException(
                "Cannot change tenant or shard mid-transaction. Active: " + existing + ", attempted: " + context
            );
        }
        CURRENT_CONTEXT.set(context);
    }

    public static Optional<TenantRequestContext> get() {
        return Optional.ofNullable(CURRENT_CONTEXT.get());
    }

    public static TenantRequestContext require() {
        return get().orElseThrow(() -> new MissingTenantContextException("No active tenant context found"));
    }

    public static void clear() {
        CURRENT_CONTEXT.remove();
    }
}
