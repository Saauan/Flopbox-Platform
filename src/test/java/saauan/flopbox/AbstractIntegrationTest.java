package saauan.flopbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import saauan.flopbox.user.User;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
public class AbstractIntegrationTest {
	protected MockMvc mockMvc;
	protected ObjectMapper objectMapper = new ObjectMapper();
	protected static String authToken;
	protected String username = "Tristan";
	protected String password = "Scooby";
	protected static boolean isAuthenticated;

	@Autowired
	private WebApplicationContext wac;

	@BeforeEach
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
	}

	protected void authenticate() throws Exception {
		if(!isAuthenticated){
			sendRequestToCreateUser(status().isCreated(), new User(username, password));
			MvcResult result = sendRequestToAuthenticate(status().isOk());
			authToken = result.getResponse().getContentAsString();
			assert authToken != null;
			isAuthenticated = true;
		}

	}

	private MvcResult sendRequestToAuthenticate(ResultMatcher expectedResponseCode) throws Exception {
		return this.mockMvc.perform(post(String.format("/token?username=%s&password=%s", username, password))
				.characterEncoding("utf-8"))
				.andExpect(expectedResponseCode)
				.andReturn();
	}

	private MvcResult sendRequestToCreateUser(ResultMatcher expectedResponseCode, User user) throws Exception {
		String jsonBody = objectMapper.writeValueAsString(user);
		return this.mockMvc.perform(MockMvcRequestBuilders.post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonBody)
				.characterEncoding("utf-8"))
				.andExpect(expectedResponseCode)
				.andReturn();
	}
}
