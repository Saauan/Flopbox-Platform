package saauan.flopbox.server;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import saauan.flopbox.AbstractIntegrationTest;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
public class ServerIntegrationTest extends AbstractIntegrationTest {

	private Server server1 = new Server("https://www.baeldung.com", 28);
	private Server server2 = new Server("https://www.baeldung2.com", 29);

	public ServerIntegrationTest() throws MalformedURLException {
	}

	@SneakyThrows
	@Override
	@BeforeEach
	public void setUp() {
		super.setUp();
		authenticate(authUser1);
		currentAuthToken = authTokens.get(authUser1);
		currentUser = authUser1;
	}

	@Test
	public void creatingServerSavesItInDatabase() throws Exception {
		Assertions.assertTrue(serverRepository.findAll().isEmpty());

		sendRequestToCreateServer(status().isCreated(), server1);
		server1.setUser(currentUser);

		Assertions.assertEquals(1, serverRepository.findAll().size());
		Server actualServer = serverRepository.findAll().stream().findFirst().orElseThrow();
		Assertions.assertEquals(server1, actualServer);
		Assertions.assertEquals(authUser1, actualServer.getUser());
	}

	@Test
	public void cannotCreateTwiceTheSameServer() throws Exception {
		sendRequestToCreateServer(status().isCreated(), server1);
		sendRequestToCreateServer(status().isForbidden(), server1);
	}

	@Test
	public void canCreateTwiceServerWithSameUrlButWithDifferentUser() throws Exception {
		sendRequestToCreateServer(status().isCreated(), server1);

		authAndChangeUser(authUser2);

		sendRequestToCreateServer(status().isCreated(), server1);

		MvcResult result = sendRequestToGetServer(status().isOk(), serverRepository.findByUser(authUser2).stream().findFirst().orElseThrow().getId());
		Server actualServer = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<Server>(){});
		Assertions.assertEquals(server1, actualServer);
	}


	@Test
	public void getServersReturnsAListOfServersBelongingToTheUser() throws Exception {
		sendRequestToCreateServer(status().isCreated(), server1);

		authAndChangeUser(authUser2);

		sendRequestToCreateServer(status().isCreated(), server1);
		sendRequestToCreateServer(status().isCreated(), server2);

		MvcResult result = sendRequestToGetServers(status().isOk());
		List<Server> actualServers = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<Server>>(){});

		Assertions.assertEquals(3, serverRepository.findAll().size());
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
	public void cannotGetAServerThatDoesNotBelongToTheUser() throws Exception {
		sendRequestToCreateServer(status().isCreated(), server1);

		authAndChangeUser(authUser2);

		sendRequestToGetServer(status().isForbidden(), serverRepository.findByUrl(server1.getUrl()).getId());
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
	public void cannotModifyAServerThatDoesNotBelongToTheUser() throws Exception {
		sendRequestToCreateServer(status().isCreated(), server1);

		authAndChangeUser(authUser2);

		sendRequestToModifyServer(status().isForbidden(), serverRepository.findByUrl(server1.getUrl()).getId(), server2);
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

	@Test
	public void cannotDeleteAServerThatDoesNotBelongToTheUser() throws Exception {
		sendRequestToCreateServer(status().isCreated(), server1);

		authAndChangeUser(authUser2);

		sendRequestToDeleteServer(status().isForbidden(), serverRepository.findByUrl(server1.getUrl()).getId());
	}

	@Test
	public void canDeleteAUserIfItHasServers() throws Exception {
		sendRequestToCreateServer(status().isCreated(), server1);
		Assertions.assertEquals(1, serverRepository.findAll().size());

		sendRequestToDeleteUser(status().isNoContent(), authUser1.getUsername());

		Assertions.assertEquals(0, serverRepository.findAll().size());
	}

}
