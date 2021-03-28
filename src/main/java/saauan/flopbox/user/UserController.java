package saauan.flopbox.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import saauan.flopbox.exceptions.ResourceAlreadyExistException;

import java.util.List;

/**
 * Controller for user related MVC Endpoints
 */
@RestController()
@RequestMapping("users")
public class UserController {

	private final UserService userService;

	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
	}

	@ApiResponse(responseCode = "200", description = "Found the users",
			content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = List.class))})
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Get all the users")
	private List<User> getAllUsers() {
		return userService.getAllUsers();
	}

	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "User created"),
			@ApiResponse(responseCode = "403", description = "User already exist"),
			@ApiResponse(responseCode = "400", description = "Bad request")
	})
	@Operation(summary = "Add a user")
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	private void addUser(@RequestBody User userToCreate) {
		try {
			this.userService.addUser(userToCreate);
		} catch (ResourceAlreadyExistException e) {
			throw new ResponseStatusException(
					HttpStatus.FORBIDDEN, e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, e.getMessage(), e);
		}
	}

	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Found the user",
					content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = User.class))}),
			@ApiResponse(responseCode = "404", description = "Not found")})
	@Operation(summary = "Get a user by its username")
	@GetMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
	private User getUser(@PathVariable String username) {
		return userService.getUser(username);
	}

	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "User deleted"),
			@ApiResponse(responseCode = "404", description = "Not found")})
	@Operation(summary = "Delete a user")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/{username}")
	private void deleteUser(@PathVariable String username) {
		userService.deleteUser(username);
	}
}
