package saauan.flopbox.server;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import saauan.flopbox.FlopBoxApplication;
import saauan.flopbox.Utils;
import saauan.flopbox.exceptions.ResourceAlreadyExistException;

import java.util.List;

/**
 * Conroller for server related endpoints
 */
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
	@Operation(summary = "Get all the servers available for the user",
			security = @SecurityRequirement(name = FlopBoxApplication.FLOPBOX_SECURITY),
			responses = {
					@ApiResponse(responseCode = "200", description = "Found the servers", content = {
							@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
									array = @ArraySchema(schema = @Schema(implementation = Server.class)))})
			}
	)
	@GetMapping
	public List<Server> getAllServers(@RequestHeader HttpHeaders headers) {
		return serverService.getAllServers(Utils.getToken(headers));
	}

	/**
	 * Adds a server to the database
	 *
	 * @param server the server to add
	 * @return the added server
	 */
	@Operation(summary = "Add a server",
			security = @SecurityRequirement(name = FlopBoxApplication.FLOPBOX_SECURITY),
			responses = {
					@ApiResponse(responseCode = "201", description = "Found the servers", content = {
							@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
									schema = @Schema(implementation = Server.class))}),
					@ApiResponse(responseCode = "403", description = "Server already exist")
			}
	)
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public Server addServer(@RequestBody Server server,
							@RequestHeader HttpHeaders headers) {
		try {
			return serverService.addServer(server, Utils.getToken(headers));
		} catch (ResourceAlreadyExistException e) {
			throw new ResponseStatusException(
					HttpStatus.FORBIDDEN, e.getMessage(), e);
		}
	}

	@Operation(summary = "Get a server available to the user",
			security = @SecurityRequirement(name = FlopBoxApplication.FLOPBOX_SECURITY),
			responses = {
					@ApiResponse(responseCode = "200", description = "Found the servers", content = {
							@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
									schema = @Schema(implementation = Server.class))}),
					@ApiResponse(responseCode = "404", description = "Server not found")
			}
	)
	@GetMapping("/{serverId}")
	public Server getServer(@PathVariable int serverId,
							@RequestHeader HttpHeaders headers) {
		return serverService.getServer(serverId, Utils.getToken(headers));
	}

	@Operation(summary = "Modify a server",
			security = @SecurityRequirement(name = FlopBoxApplication.FLOPBOX_SECURITY),
			responses = {
					@ApiResponse(responseCode = "200", description = "Server modified"),
					@ApiResponse(responseCode = "404", description = "Server not found")
			}
	)
	@PutMapping("/{serverId}")
	@ResponseStatus(HttpStatus.OK)
	public void modifyServer(@RequestBody Server server,
							 @PathVariable int serverId,
							 @RequestHeader HttpHeaders headers) {
		serverService.modifyServer(server, serverId, Utils.getToken(headers));
	}

	@Operation(summary = "Delete a server",
			security = @SecurityRequirement(name = FlopBoxApplication.FLOPBOX_SECURITY),
			responses = {
					@ApiResponse(responseCode = "204", description = "Server deleted"),
					@ApiResponse(responseCode = "404", description = "Server not found")
			}
	)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/{serverId}")
	public void deleteServer(@PathVariable int serverId,
							 @RequestHeader HttpHeaders headers) {
		serverService.deleteServer(serverId, Utils.getToken(headers));
	}
}
