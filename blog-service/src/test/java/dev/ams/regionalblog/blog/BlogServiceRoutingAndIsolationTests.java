package dev.ams.regionalblog.blog;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ams.regionalblog.blog.post.PostDtos;
import dev.ams.regionalblog.blog.tenant.DirectoryClient;
import dev.ams.regionalblog.blog.tenant.TenantContext;
import dev.ams.regionalblog.blog.tenant.TenantRequestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BlogServiceRoutingAndIsolationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DirectoryClient directoryClient;

    @BeforeEach
    void setUp() {
        // tenant-eu-001 -> EU shard-1
        Mockito.when(directoryClient.getPlacement(eq("tenant-eu-001")))
                .thenReturn(Optional.of(new DirectoryClient.PlacementRecord("tenant-eu-001", "eu", "shard-1", "ACTIVE")));

        // tenant-eu-002 -> EU shard-2
        Mockito.when(directoryClient.getPlacement(eq("tenant-eu-002")))
                .thenReturn(Optional.of(new DirectoryClient.PlacementRecord("tenant-eu-002", "eu", "shard-2", "ACTIVE")));

        // tenant-eu-003 -> EU shard-1 (shares shard-1 with tenant-eu-001 to test Hibernate logical discriminator isolation)
        Mockito.when(directoryClient.getPlacement(eq("tenant-eu-003")))
                .thenReturn(Optional.of(new DirectoryClient.PlacementRecord("tenant-eu-003", "eu", "shard-1", "ACTIVE")));

        // tenant-me-001 -> Middle East shard-1 (to test regional boundary rejection)
        Mockito.when(directoryClient.getPlacement(eq("tenant-me-001")))
                .thenReturn(Optional.of(new DirectoryClient.PlacementRecord("tenant-me-001", "me", "shard-1", "ACTIVE")));

        // tenant-eu-suspended -> EU shard-1 with SUSPENDED status
        Mockito.when(directoryClient.getPlacement(eq("tenant-eu-suspended")))
                .thenReturn(Optional.of(new DirectoryClient.PlacementRecord("tenant-eu-suspended", "eu", "shard-1", "SUSPENDED")));

        // unknown tenant
        Mockito.when(directoryClient.getPlacement(eq("unknown-tenant")))
                .thenReturn(Optional.empty());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldCreateAndReadPostOnShard1() throws Exception {
        PostDtos.CreatePostRequest createReq = new PostDtos.CreatePostRequest(
                "EU Shard 1 Post",
                "Content from tenant-eu-001 on shard-1"
        );

        MvcResult createResult = mockMvc.perform(post("/api/posts")
                        .header("X-Tenant-Id", "tenant-eu-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("EU Shard 1 Post"))
                .andExpect(jsonPath("$.tenantId").value("tenant-eu-001"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        PostDtos.PostResponse createdPost = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                PostDtos.PostResponse.class
        );

        mockMvc.perform(get("/api/posts/" + createdPost.id())
                        .header("X-Tenant-Id", "tenant-eu-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdPost.id().toString()))
                .andExpect(jsonPath("$.title").value("EU Shard 1 Post"));

        // Context must be cleared after request
        assertThat(TenantContext.get()).isEmpty();
    }

    @Test
    void shouldCreatePostOnShard2ForEu002() throws Exception {
        PostDtos.CreatePostRequest createReq = new PostDtos.CreatePostRequest(
                "EU Shard 2 Post",
                "Content from tenant-eu-002 on shard-2"
        );

        mockMvc.perform(post("/api/posts")
                        .header("X-Tenant-Id", "tenant-eu-002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value("tenant-eu-002"));

        mockMvc.perform(get("/internal/routing-info")
                        .header("X-Tenant-Id", "tenant-eu-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("tenant-eu-002"))
                .andExpect(jsonPath("$.shardId").value("shard-2"))
                .andExpect(jsonPath("$.region").value("eu"));
    }

    @Test
    void shouldEnforceIsolationBetweenTenantsSharingSamePhysicalShard() throws Exception {
        // Step 1: tenant-eu-001 creates post on shard-1
        PostDtos.CreatePostRequest createReq = new PostDtos.CreatePostRequest(
                "Private EU-001 Post",
                "Confidential body"
        );

        MvcResult createResult = mockMvc.perform(post("/api/posts")
                        .header("X-Tenant-Id", "tenant-eu-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        PostDtos.PostResponse postEu1 = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                PostDtos.PostResponse.class
        );

        // Step 2: tenant-eu-003 is also on shard-1, but must NOT be able to read tenant-eu-001's post
        mockMvc.perform(get("/api/posts/" + postEu1.id())
                        .header("X-Tenant-Id", "tenant-eu-003"))
                .andExpect(status().isNotFound());

        // Step 3: tenant-eu-003 must not see it in the list
        mockMvc.perform(get("/api/posts")
                        .header("X-Tenant-Id", "tenant-eu-003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.id == '" + postEu1.id() + "')]").doesNotExist());

        // Step 4: tenant-eu-003 cannot patch it
        PostDtos.UpdatePostRequest patchReq = new PostDtos.UpdatePostRequest("Hacked Title", null, null);
        mockMvc.perform(patch("/api/posts/" + postEu1.id())
                        .header("X-Tenant-Id", "tenant-eu-003")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchReq)))
                .andExpect(status().isNotFound());

        // Step 5: tenant-eu-003 cannot delete it
        mockMvc.perform(delete("/api/posts/" + postEu1.id())
                        .header("X-Tenant-Id", "tenant-eu-003"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectTenantAssignedToAnotherRegion() throws Exception {
        // Middle East tenant sent directly to EU app deployment must be rejected
        mockMvc.perform(get("/api/posts")
                        .header("X-Tenant-Id", "tenant-me-001"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Cross-region request rejected")));
    }

    @Test
    void shouldRejectSuspendedTenant() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .header("X-Tenant-Id", "tenant-eu-suspended"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Tenant is not active")));
    }

    @Test
    void shouldReturn404ForUnknownTenant() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .header("X-Tenant-Id", "unknown-tenant"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Unknown tenant")));
    }

    @Test
    void shouldRejectRequestWithoutTenantIdHeader() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing required X-Tenant-Id header"));
    }

    @Test
    void shouldThrowWhenAttemptingToChangeTenantOrShardMidTransaction() {
        TenantRequestContext ctx1 = new TenantRequestContext("tenant-eu-001", "eu", "shard-1");
        TenantRequestContext ctx2 = new TenantRequestContext("tenant-eu-002", "eu", "shard-2");

        TenantContext.set(ctx1);

        assertThatThrownBy(() -> TenantContext.set(ctx2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot change tenant or shard mid-transaction");
    }
}
