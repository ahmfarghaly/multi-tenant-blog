package dev.ams.regionalblog.gateway;

import dev.ams.regionalblog.gateway.domain.TenantPlacement;
import dev.ams.regionalblog.gateway.domain.TenantPlacementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GatewayApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantPlacementRepository placementRepository;

    @Test
    void shouldSeedDefaultTenantsViaFlyway() {
        assertThat(placementRepository.count()).isGreaterThanOrEqualTo(6);

        TenantPlacement eu1 = placementRepository.findById("tenant-eu-001").orElseThrow();
        assertThat(eu1.getRegion()).isEqualTo("eu");
        assertThat(eu1.getShardId()).isEqualTo("shard-1");
        assertThat(eu1.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldGetPlacementByTenantId() throws Exception {
        mockMvc.perform(get("/internal/placements/tenant-eu-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("tenant-eu-001"))
                .andExpect(jsonPath("$.region").value("eu"))
                .andExpect(jsonPath("$.shardId").value("shard-1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturn404ForUnknownTenant() throws Exception {
        mockMvc.perform(get("/internal/placements/non-existent-tenant"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpsertTenantPlacement() throws Exception {
        String payload = """
                {
                    "region": "eu",
                    "shardId": "shard-1",
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(put("/internal/placements/tenant-eu-003")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("tenant-eu-003"))
                .andExpect(jsonPath("$.region").value("eu"))
                .andExpect(jsonPath("$.shardId").value("shard-1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        assertThat(placementRepository.findById("tenant-eu-003")).isPresent();
    }

    @Test
    void shouldRejectProxyRequestWithoutTenantIdHeader() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isBadRequest());
    }
}
