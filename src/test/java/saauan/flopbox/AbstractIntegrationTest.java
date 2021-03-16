package saauan.flopbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import saauan.flopbox.server.Server;
import saauan.flopbox.user.User;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
public class AbstractIntegrationTest {
	protected MockMvc mockMvc;
	protected ObjectMapper objectMapper = new ObjectMapper();

	protected static Map<User, String> authTokens = new HashMap<>();
	protected User authUser1 = new User("Tristan", "Scooby");
	protected User authUser2 = new User("Anthony", "Doo");
	protected static Map<User, Boolean> isAuthenticated = new HashMap<>();
	public static final String BEARER = "Bearer ";
	protected final String serversUrl = "/servers";
	protected String currentAuthToken;
	protected User currentUser;
	private final String usersUrl = "/users";

	@Autowired
	private WebApplicationContext wac;

	@BeforeEach
	public void setUp() throws Exception {
		mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
	}

	protected void authenticate(User user) throws Exception {
		if(!isAuthenticated.containsKey(user)){
			sendRequestToCreateUser(status().isCreated(), user);
			MvcResult result = sendRequestToAuthenticate(status().isOk(), user);
			authTokens.put(user, result.getResponse().getContentAsString());
			isAuthenticated.put(user, true);
		}
	}

	private MvcResult sendRequestToAuthenticate(ResultMatcher expectedResponseCode, User user) throws Exception {
		return this.mockMvc.perform(post(String.format("/token?username=%s&password=%s", user.getUsername(), user.getPassword()))
				.characterEncoding("utf-8"))
				.andExpect(expectedResponseCode)
				.andReturn();
	}

	protected MvcResult sendRequestToCreateUser(ResultMatcher expectedResponseCode, User user) throws Exception {
		String jsonBody = objectMapper.writeValueAsString(user);
		return this.mockMvc.perform(MockMvcRequestBuilders.post(usersUrl)
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonBody)
				.characterEncoding("utf-8"))
				.andExpect(expectedResponseCode)
				.andReturn();
	}

	protected MvcResult sendRequestToGetUsers(ResultMatcher expectedResponseCode) throws Exception {
		return this.mockMvc.perform(MockMvcRequestBuilders.get(usersUrl)
				.characterEncoding("utf-8"))
				.andExpect(expectedResponseCode)
				.andReturn();
	}

	protected MvcResult sendRequestToGetUser(ResultMatcher expectedResponseCode, String username) throws Exception {
		return this.mockMvc.perform(get(usersUrl + "/" + username)
				.characterEncoding("utf-8"))
				.andExpect(expectedResponseCode)
				.andReturn();
	}

	protected MvcResult sendRequestToDeleteUser(ResultMatcher expectedResponseCode, String username) throws Exception {
		return this.mockMvc.perform(delete(usersUrl + "/" + username)
				.characterEncoding("utf-8"))
				.andExpect(expectedResponseCode)
				.andReturn();
	}

	protected void authAndChangeUser(User user) throws Exception {
		authenticate(user);
		currentUser = user;
		currentAuthToken = authTokens.get(user);
	}

	protected MvcResult sendRequestToCreateServer(ResultMatcher expectedResponseCode, Server server) throws Exception {
		String jsonBody = objectMapper.writeValueAsString(server);
		server.setUser(currentUser);
		return this.mockMvc.perform(MockMvcRequestBuilders.post(serversUrl)
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonBody)
				.characterEncoding("utf-8")
				.header(HttpHeaders.AUTHORIZATION, BEARER + currentAuthToken))
				.andExpect(expectedResponseCode)
				.andReturn();
	}

	protected MvcResult sendRequestToModifyServer(ResultMatcher expectedResponseCode, int id, Server server)
			throws Exception {
		String jsonBody = objectMapper.writeValueAsString(server);
		server.setUser(currentUser);
		return this.mockMvc.perform(MockMvcRequestBuilders.put(serversUrl + "/" + id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonBody)
				.characterEncoding("utf-8")
				.header(HttpHeaders.AUTHORIZATION, BEARER + currentAuthToken))
				.andExpect(expectedResponseCode)
				.andReturn();
	}

	protected MvcResult sendRequestToGetServers(ResultMatcher expectedResponseCode) throws Exception {
		return this.mockMvc.perform(get(serversUrl)
				.characterEncoding("utf-8")
				.header(HttpHeaders.AUTHORIZATION, BEARER + currentAuthToken))
				.andExpect(expectedResponseCode)
				.andReturn();
	}

	protected MvcResult sendRequestToGetServer(ResultMatcher expectedResponseCode, int serverId) throws Exception {
		return this.mockMvc.perform(get(serversUrl + "/" + serverId)
				.characterEncoding("utf-8")
				.header(HttpHeaders.AUTHORIZATION, BEARER + currentAuthToken))
				.andExpect(expectedResponseCode)
				.andReturn();
	}

	protected MvcResult sendRequestToDeleteServer(ResultMatcher expectedResponseCode, int serverId) throws Exception {
		return this.mockMvc.perform(delete(serversUrl + "/" + serverId)
				.characterEncoding("utf-8")
				.header(HttpHeaders.AUTHORIZATION, BEARER + currentAuthToken))
				.andExpect(expectedResponseCode)
				.andReturn();
	}
}
