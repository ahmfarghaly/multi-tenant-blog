package dev.ams.regionalblog.gateway.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantPlacementRepository extends JpaRepository<TenantPlacement, String> {
    List<TenantPlacement> findAllByOrderByTenantIdAsc();
}
