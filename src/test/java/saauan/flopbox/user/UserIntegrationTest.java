package saauan.flopbox.user;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import saauan.flopbox.AbstractIntegrationTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class UserIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private UserRepository userRepository;
	private User user1 = new User("saauan", "marshmallow");
	private User user2 = new User("dootris", "scoob");

	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		userRepository.deleteAll();
	}

	@Test
	public void creatingUserSavesItInDatabase() throws Exception {
		Assertions.assertTrue(userRepository.findAll().isEmpty());

		sendRequestToCreateUser(status().isCreated(), user1);

		Assertions.assertEquals(1, userRepository.findAll().size());
		Assertions.assertEquals(user1, userRepository.findAll().stream().findFirst().orElseThrow());
	}

	@Test
	public void cannotCreateTwoUsersWithTheSameUsername() throws Exception {
		sendRequestToCreateUser(status().isCreated(), user1);
		User user2 = new User(user1.getUsername(), "NotSamePassword");
		sendRequestToCreateUser(status().isForbidden(), user2);
	}

	@Test
	public void cannotCreateUserWithNonAlphanumericUsername() throws Exception {
		User userToCreate = new User("*-?", "insult");
		sendRequestToCreateUser(status().isBadRequest(), userToCreate);
	}

	// TODO: Cannot create user if arguments are incorrect

	@Test
	public void getUsersReturnsAListOfUsers() throws Exception {
		sendRequestToCreateUser(status().isCreated(), user1);
		sendRequestToCreateUser(status().isCreated(), user2);

		MvcResult result = sendRequestToGetUsers(status().isOk());
		List<User> actualUsers = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<User>>(){});

		Assertions.assertEquals(2, actualUsers.size());
		Assertions.assertEquals(Set.of(user1, user2), new HashSet<>(actualUsers));
	}

	@Test
	public void getUserReturnsCorrectUser() throws Exception {
		sendRequestToCreateUser(status().isCreated(), user1);
		sendRequestToCreateUser(status().isCreated(), user2);
		User anyUser = userRepository.findAll().stream().findAny().orElseThrow();

		MvcResult result = sendRequestToGetUser(status().isOk(), anyUser.getUsername());

		User actualUser = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<User>(){});
		Assertions.assertEquals(anyUser, actualUser);
	}

	@Test
	public void getUserFailsIfUserDoesNotExist() throws Exception {
		sendRequestToGetUser(status().isNotFound(), "notexist");
	}

	@Test
	public void deleteUserDeletesIt() throws Exception {
		sendRequestToCreateUser(status().isCreated(), user1);
		sendRequestToCreateUser(status().isCreated(), user2);
		Assertions.assertEquals(2, userRepository.findAll().size());

		sendRequestToDeleteUser(status().isNoContent(), user1.getUsername());

		Assertions.assertEquals(1, userRepository.findAll().size());
		Assertions.assertEquals(user2, userRepository.findAll().stream().findAny().orElseThrow());
	}

	@Test
	public void cannotDeleteUserIfItDoesNotExist() throws Exception {
		sendRequestToDeleteUser(status().isNotFound(), "NotExist");
	}

	@Test
	public void canDeleteUserAndItsServers() throws Exception {
		authenticate(authUser1);
	}

}
