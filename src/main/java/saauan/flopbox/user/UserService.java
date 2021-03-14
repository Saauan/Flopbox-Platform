package saauan.flopbox.user;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import saauan.flopbox.Utils;
import saauan.flopbox.exceptions.ResourceAlreadyExistException;

import java.util.List;

@Service
@CommonsLog
public class UserService {
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
	 */
	public User getUser(String username) {
		return Utils.findObjectOrThrow(userRepository, username, log);
	}
}
