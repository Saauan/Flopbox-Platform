package saauan.flopbox.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.http.RequestEntity.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
public class ServerIntegrationTest extends saauan.flopbox.AbstractIntegrationTest {

	@Autowired
	private ServerRepository serverRepository;
	private final String serversUrl = "/servers";
	private Server server1 = new Server(new URL("https://www.baeldung.com"), 28);
	private Server server2 = new Server(new URL("https://www.baeldung2.com"), 29);

	public ServerIntegrationTest() throws MalformedURLException {
	}

	@Override
	@BeforeEach
	public void setUp() {
		serverRepository.deleteAll();
	}

	@Test
	public void creatingServerSavesItInDatabase() throws Exception {
		Assertions.assertTrue(serverRepository.findAll().isEmpty());

		sendRequestToCreateServer(status().isCreated(), server1);

		Assertions.assertEquals(1, serverRepository.findAll().size());
		Assertions.assertEquals(server1, serverRepository.findAll().stream().findFirst().orElseThrow());
	}

	@Test
	public void cannotCreateTwiceTheSameServer() throws Exception {
		sendRequestToCreateServer(status().isCreated(), server1);
		sendRequestToCreateServer(status().isForbidden(), server1);
	}

	@Test
	public void getServersReturnsAListOfServers() throws Exception {
		sendRequestToCreateServer(status().isCreated(), server1);
		sendRequestToCreateServer(status().isCreated(), server2);

		MvcResult result = sendRequestToGetServers(status().isOk());
		List<Server> actualServers = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<Server>>(){});

		Assertions.assertEquals(2, actualServers.size());
		Assertions.assertEquals(Set.of(server1, server2), new HashSet<>(actualServers));
	}

	@Test
	public void getServerReturnsCorrectServer() throws Exception {
		sendRequestToCreateServer(status().isCreated(), server1);
		sendRequestToCreateServer(status().isCreated(), server2);
		Server anyServer = serverRepository.findAll().stream().findAny().orElseThrow();

		MvcResult result = sendRequestToGetServer(status().isOk(), anyServer.getId());

		Server actualServer = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<Server>(){});
		Assertions.assertEquals(anyServer, actualServer);
	}

	@Test
	public void getServerFailsIfServerDoesNotExist() throws Exception {
		sendRequestToGetServer(status().isNotFound(), 1);
	}

	@Test
	public void modifyServerModifiesTheServer() throws Exception {
		sendRequestToCreateServer(status().isCreated(), server1);
		Server anyServer = serverRepository.findAll().stream().findAny().orElseThrow();

		sendRequestToModifyServer(status().isOk(), anyServer.getId(), server2);
		System.err.println("End of request");

		Assertions.assertEquals(1, serverRepository.findAll().size());
		Server actualServer = serverRepository.findAll().stream().findAny().orElseThrow();
		Assertions.assertEquals(server2, actualServer);
	}

	@Test
	public void cannotModifyAServerIfNotItDoesNotExist() throws Exception {
		sendRequestToModifyServer(status().isNotFound(), 1, server2);
	}

	@Test
	public void deleteServerDeletesIt() throws Exception {
		sendRequestToCreateServer(status().isCreated(), server1);
		sendRequestToCreateServer(status().isCreated(), server2);
		Assertions.assertEquals(2, serverRepository.findAll().size());

		sendRequestToDeleteServer(status().isNoContent(), serverRepository.findByUrl(server1.getUrl()).getId());

		Assertions.assertEquals(1, serverRepository.findAll().size());
		Assertions.assertEquals(server2, serverRepository.findAll().stream().findAny().orElseThrow());
	}

	@Test
	public void cannotDeleteServerIfItDoesNotExist() throws Exception {
		sendRequestToDeleteServer(status().isNotFound(), 1);
	}

	private MvcResult sendRequestToCreateServer(ResultMatcher expectedResponseCode, Server server) throws Exception {
		String jsonBody = objectMapper.writeValueAsString(server);
		return this.mockMvc.perform(MockMvcRequestBuilders.post(serversUrl)
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonBody)
				.characterEncoding("utf-8"))
				.andExpect(expectedResponseCode)
				.andReturn();
	}

	private MvcResult sendRequestToModifyServer(ResultMatcher expectedResponseCode, int id, Server server) throws Exception {
		String jsonBody = objectMapper.writeValueAsString(server);
		return this.mockMvc.perform(MockMvcRequestBuilders.patch(serversUrl + "/" + id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonBody)
				.characterEncoding("utf-8"))
				.andExpect(expectedResponseCode)
				.andReturn();
	}

	private MvcResult sendRequestToGetServers(ResultMatcher expectedResponseCode) throws Exception {
		return this.mockMvc.perform(get(serversUrl)
				.characterEncoding("utf-8"))
				.andExpect(expectedResponseCode)
				.andReturn();
	}

	private MvcResult sendRequestToGetServer(ResultMatcher expectedResponseCode, int serverId) throws Exception {
		return this.mockMvc.perform(get(serversUrl + "/" + serverId)
				.characterEncoding("utf-8"))
				.andExpect(expectedResponseCode)
				.andReturn();
	}

	private MvcResult sendRequestToDeleteServer(ResultMatcher expectedResponseCode, int serverId) throws Exception {
		return this.mockMvc.perform(delete(serversUrl + "/" + serverId)
				.characterEncoding("utf-8"))
				.andExpect(expectedResponseCode)
				.andReturn();
	}
}
