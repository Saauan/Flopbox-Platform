package saauan.flopbox.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import saauan.flopbox.exceptions.ResourceAlreadyExistException;

import java.util.List;

@RestController()
@RequestMapping("users")
public class UserController {

	private final UserService userService;

	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	private List<User> getAllUsers(){
		return userService.getAllUsers();
	}

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	private void addUser(@RequestBody User userToCreate){
		try {
			this.userService.addUser(userToCreate);
		} catch (ResourceAlreadyExistException e) {
			throw new ResponseStatusException(
					HttpStatus.FORBIDDEN, e.getMessage(), e);
		}
	}

	@GetMapping("/{username}")
	private User getUser(@PathVariable String username){
		return userService.getUser(username);
	}

	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/{username}")
	private void deleteUser(@PathVariable String username){

	}
}
