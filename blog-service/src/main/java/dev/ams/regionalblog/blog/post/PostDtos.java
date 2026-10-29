package dev.ams.regionalblog.blog.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class PostDtos {

    private PostDtos() {
    }

    public record CreatePostRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title cannot exceed 200 characters")
        String title,

        @NotBlank(message = "Body is required")
        String body
    ) {}

    public record UpdatePostRequest(
        @Size(max = 200, message = "Title cannot exceed 200 characters")
        String title,

        String body,

        PostStatus status
    ) {}

    public record PostResponse(
        UUID id,
        String tenantId,
        String title,
        String body,
        PostStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {
        public static PostResponse fromEntity(BlogPost post) {
            return new PostResponse(
                post.getId(),
                post.getTenantId(),
                post.getTitle(),
                post.getBody(),
                post.getStatus(),
                post.getCreatedAt(),
                post.getUpdatedAt()
            );
        }
    }
}
