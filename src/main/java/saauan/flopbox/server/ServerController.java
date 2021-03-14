package saauan.flopbox.server;

import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import saauan.flopbox.exceptions.ResourceAlreadyExistException;

import java.util.List;

@RestController()
@RequestMapping("servers")
public class ServerController {

	private final ServerService serverService;

	@Autowired
	public ServerController(ServerService serverService) {
		this.serverService = serverService;
	}

	/**
	 * Returns a list of all the servers
	 *
	 * @return Returns a list of all the servers
	 */
	@GetMapping
	public List<Server> getAllServers(@RequestHeader HttpHeaders headers){
		System.err.println(StringUtils.removeStart(headers.get(HttpHeaders.AUTHORIZATION).get(0), "Bearer").trim());
		return serverService.getAllServers();
	}

	/**
	 * Adds a server to the database
	 *
	 * @param server the server to add
	 */
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public void addServer(@RequestBody Server server) {
		try {
			serverService.addServer(server);
		} catch (ResourceAlreadyExistException e) {
			throw new ResponseStatusException(
					HttpStatus.FORBIDDEN, e.getMessage(), e);
		}
	}

	@GetMapping("/{serverId}")
	public Server getServer(@PathVariable int serverId) {
		return serverService.getServer(serverId);
	}

	@PatchMapping("/{serverId}")
	public void modifyServer(@RequestBody Server server, @PathVariable int serverId){
		serverService.modifyServer(server, serverId);
	}

	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/{serverId}")
	public void deleteServer(@PathVariable int serverId) {
		serverService.deleteServer(serverId);
	}
}
