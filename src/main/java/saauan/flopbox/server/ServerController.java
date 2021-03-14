package saauan.flopbox.server;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController()
@RequestMapping("servers")
@CommonsLog
public class ServerController {

	private final ServerService serverService;

	@Autowired
	public ServerController(ServerService serverService) {
		this.serverService = serverService;
	}

	@GetMapping
	public List<Server> getAllServers(){
		return null;
	}

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public void addServer(@RequestBody Server server) {
		try {
			serverService.addServer(server);
		} catch (ServerAlreadyExistException e) {
			throw new ResponseStatusException(
					HttpStatus.FORBIDDEN, e.getMessage(), e);
		}
	}

	@GetMapping("/{serverId}")
	public Server getServer(@PathVariable int serverId) {
		return null;
	}

	@PatchMapping("/{serverId}")
	public void modifyServer(@RequestBody Server server, @PathVariable int serverId){

	}

	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/{serverId}")
	public void deleteServer(@PathVariable int serverId) {

	}
}
