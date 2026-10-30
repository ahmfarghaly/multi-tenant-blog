package dev.ams.regionalblog.blog.post;

import dev.ams.regionalblog.blog.tenant.TenantContext;
import dev.ams.regionalblog.blog.tenant.TenantRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class BlogPostService {

    private static final Logger log = LoggerFactory.getLogger(BlogPostService.class);

    private final BlogPostRepository repository;

    public BlogPostService(BlogPostRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PostDtos.PostResponse createPost(PostDtos.CreatePostRequest request) {
        TenantRequestContext context = TenantContext.require();
        log.info("Creating post for tenant: {} on shard: {}", context.tenantId(), context.shardId());

        BlogPost post = new BlogPost(request.title(), request.body(), PostStatus.DRAFT);
        post.setTenantId(context.tenantId());

        BlogPost saved = repository.save(post);
        return PostDtos.PostResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<PostDtos.PostResponse> listPosts() {
        TenantRequestContext context = TenantContext.require();
        log.debug("Listing posts for tenant: {} on shard: {}", context.tenantId(), context.shardId());

        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(PostDtos.PostResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PostDtos.PostResponse getPost(UUID id) {
        TenantRequestContext context = TenantContext.require();
        log.debug("Fetching post: {} for tenant: {}", id, context.tenantId());

        BlogPost post = repository.findByIdAndTenantId(id, context.tenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found: " + id));

        return PostDtos.PostResponse.fromEntity(post);
    }

    @Transactional
    public PostDtos.PostResponse updatePost(UUID id, PostDtos.UpdatePostRequest request) {
        TenantRequestContext context = TenantContext.require();
        log.info("Updating post: {} for tenant: {}", id, context.tenantId());

        BlogPost post = repository.findByIdAndTenantId(id, context.tenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found: " + id));

        if (request.title() != null && !request.title().isBlank()) {
            post.setTitle(request.title());
        }
        if (request.body() != null) {
            post.setBody(request.body());
        }
        if (request.status() != null) {
            post.setStatus(request.status());
        }

        BlogPost updated = repository.save(post);
        return PostDtos.PostResponse.fromEntity(updated);
    }

    @Transactional
    public void deletePost(UUID id) {
        TenantRequestContext context = TenantContext.require();
        log.info("Deleting post: {} for tenant: {}", id, context.tenantId());

        BlogPost post = repository.findByIdAndTenantId(id, context.tenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found: " + id));

        repository.delete(post);
    }
}
