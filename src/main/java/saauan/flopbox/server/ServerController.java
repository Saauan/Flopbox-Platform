package saauan.flopbox.server;

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
		return serverService.getAllServers(getToken(headers));
	}

	public static String getToken(HttpHeaders headers) {
		List<String> auth = headers.get(HttpHeaders.AUTHORIZATION);
		assert auth != null;
		return StringUtils.removeStart(auth.stream().findFirst().orElseThrow(), "Bearer").trim();
	}

	/**
	 * Adds a server to the database
	 *
	 * @param server the server to add
	 */
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public void addServer(@RequestBody Server server,
						  @RequestHeader HttpHeaders headers) {
		try {
			serverService.addServer(server, getToken(headers));
			// Renvoyer id du serveur
		} catch (ResourceAlreadyExistException e) {
			throw new ResponseStatusException(
					HttpStatus.FORBIDDEN, e.getMessage(), e);
		}
	}

	@GetMapping("/{serverId}")
	public Server getServer(@PathVariable int serverId,
							@RequestHeader HttpHeaders headers) {
		return serverService.getServer(serverId, getToken(headers));
	}

	@PutMapping("/{serverId}")
	public void modifyServer(@RequestBody Server server,
							 @PathVariable int serverId,
							 @RequestHeader HttpHeaders headers){
		serverService.modifyServer(server, serverId, getToken(headers));
	}

	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/{serverId}")
	public void deleteServer(@PathVariable int serverId,
							 @RequestHeader HttpHeaders headers) {
		serverService.deleteServer(serverId, getToken(headers));
	}
}
