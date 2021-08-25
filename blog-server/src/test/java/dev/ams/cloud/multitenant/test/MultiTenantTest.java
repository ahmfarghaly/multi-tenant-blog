package dev.ams.cloud.multitenant.test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.ams.cloud.multitenant.model.Post;
import dev.ams.cloud.multitenant.model.User;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class MultiTenantTest {

	@Autowired
	WebApplicationContext context;
	
	MockMvc mockMvc;
	
	EntityFactory entityFactory = new EntityFactory();
	
	@BeforeAll
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}
	
	void testSavePost() {
		try {
			ObjectMapper om = new ObjectMapper();
			User user = entityFactory.createUser();
			Post post = entityFactory.createPost(user);
			
			mockMvc.perform(post("/posts").header("X-TENANT-ID", "").content(om.writeValueAsString(post)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$", notNullValue()));
		} catch (Exception e) {
			fail(e);
		}
	}
	
	void testGetPost_for_rightTenant() {
		try {
			mockMvc.perform(get("/posts").header("X-TENANT-ID", ""))
					.andExpect(jsonPath("$", notNullValue()))
					.andDo(MockMvcResultHandlers.print());
		} catch (Exception e) {
			fail(e);
		}
	}
	
	void testGetPost_for_wrongTenant() {
		
	}
}
