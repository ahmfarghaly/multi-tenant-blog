package dev.ams.regionalblog.blog.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, UUID> {

    List<BlogPost> findAllByOrderByCreatedAtDesc();

    Optional<BlogPost> findByIdAndTenantId(UUID id, String tenantId);
}
