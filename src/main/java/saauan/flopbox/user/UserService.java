package saauan.flopbox.user;

import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;
import saauan.flopbox.Utils;
import saauan.flopbox.exceptions.ResourceAlreadyExistException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@CommonsLog
public class UserService {

	// TODO: Refactor into interfaces

	private final UserRepository userRepository;
	private final static String alphanumericRegex = "[a-zA-Z0-9]+";

	@Autowired
	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * Adds a user to the database
	 * @param user the user to create
	 * @throws IllegalArgumentException if the username or password are incorrect
	 * @throws ResourceAlreadyExistException if the user already exists
	 */
	public void addUser(User user) {
		if (isUserDataCorrect(user)) {
			if(this.userRepository.findById(user.getUsername()).isPresent()) {
				Utils.logAndThrow(log, ResourceAlreadyExistException.class, String.format("The user %s already exists !", user));
			}
			this.userRepository.save(user);
		} else {
			Utils.logAndThrow(log, IllegalArgumentException.class, "Some field from user were incorrect or missing.");
		}
	}

	private boolean isUsernameAlphanumeric(String username) {
		return username.matches(alphanumericRegex);
	}

	private boolean isUserDataCorrect(User user) {
		return !user.getUsername().isBlank() &&
				!user.getPassword().isBlank() &&
				isUsernameAlphanumeric(user.getUsername());
	}

	/**
	 * Returns all users
	 * @return a list containing all the users
	 */
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	/**
	 * Returns the user with the same username as `username`
	 * @param username the username of the user
	 * @return a user with a corresponding username
	 * @throws saauan.flopbox.exceptions.ResourceNotFoundException if the user if not found
	 */
	public User getUser(String username) {
		return Utils.findObjectOrThrow(userRepository, username, log);
	}

	/**
	 * Deletes a user from the database
	 * @param username the username of the user to delete
	 *
	 * @throws saauan.flopbox.exceptions.ResourceNotFoundException if the user if not found
	 */
	public void deleteUser(String username) {
		User user = Utils.findObjectOrThrow(userRepository, username, log);
		userRepository.delete(user);
	}

	public String login(String username, String password) {
		Optional<User> optionalUser = userRepository.login(username, password);
		if(optionalUser.isPresent()) {
			String token = UUID.randomUUID().toString();
			User user = optionalUser.get();
			user.setToken(token);
			userRepository.save(user);
			return token;
		}
		log.error("No user was found while login in");
		return StringUtils.EMPTY;
	}

	public Optional<org.springframework.security.core.userdetails.User> findByToken(String token) {
		Optional<User> optionalUser = userRepository.findByToken(token);
		if(optionalUser.isPresent()) {
			User appUser = optionalUser.get();
			org.springframework.security.core.userdetails.User user = new org.springframework.security.core.userdetails.User(appUser.getUsername(), appUser.getPassword(), true, true, true, true,
					AuthorityUtils.createAuthorityList("USER"));
			return Optional.of(user);
		}
		return Optional.empty();
	}

	public Optional<User> findUserByToken(String token) {
		Optional<org.springframework.security.core.userdetails.User> springUser = findByToken(token);
		if(springUser.isPresent()) {
			 return userRepository.findByUsernameAndPassword(springUser.get().getUsername(), springUser.get().getPassword());
		} else {
			return Optional.empty();
		}
	}
}
