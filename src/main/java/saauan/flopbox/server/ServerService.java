package saauan.flopbox.server;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import saauan.flopbox.Utils;
import saauan.flopbox.exceptions.ResourceAlreadyExistException;
import saauan.flopbox.exceptions.ResourceNotFoundException;
import saauan.flopbox.user.User;
import saauan.flopbox.user.UserService;

import java.util.List;

@Service
@CommonsLog
public class ServerService {

	private final ServerRepository serverRepository;
	private final UserService userService;

	@Autowired
	public ServerService(ServerRepository serverRepository, UserService userService) {
		this.serverRepository = serverRepository;
		this.userService = userService;
	}

	/**
	 * Adds a server to the database
	 *
	 * @param server the server to add
	 * @throws ResourceAlreadyExistException if the server already exists
	 */
	public void addServer(Server server, String token) {
		log.info(String.format("Adding server %s", server));
		User user = userService.findUserByToken(token).orElseThrow();
		if (this.serverRepository.findByUserAndUrl(user, server.getUrl()) != null) {
			Utils.logAndThrow(log, ResourceAlreadyExistException.class, String.format("Server %s already exists", server.getUrl()));
		}
		server.setUser(user);
		serverRepository.save(server);
	}

	/**
	 * Returns a list of all the servers
	 *
	 * @return Returns a list of all the servers
	 */
	public List<Server> getAllServers(String token) {
		log.info("Getting all servers");
		User user = userService.findUserByToken(token).orElseThrow();
		return serverRepository.findByUser(user);
	}

	/**
	 * Gets the server from the database
	 *
	 * @param serverId the id of the server
	 * @return the server corresponding to `serverId`
	 * @throws ResourceNotFoundException if the server does not exist
	 */
	public Server getServer(int serverId, String token) {
		log.info(String.format("Getting server %s", serverId));
		User user = userService.findUserByToken(token).orElseThrow();
		Server server = Utils.findObjectOrThrow(serverRepository, serverId, log);
		throwIfServerDoesNotBelongToUser(server, user);
		return server;

	}

	/**
	 * Modifies a server in the database
	 *
	 * @param serverModifications a server object containing changes to apply to the server
	 * @param serverId the id of the server to modify
	 * @throws ResourceNotFoundException if the server does not exist
	 */
	public void modifyServer(Server serverModifications, int serverId, String token) {
		log.info(String.format("Modifying server %s", serverId));
		User user = userService.findUserByToken(token).orElseThrow();
		Server serverToModify = Utils.findObjectOrThrow(serverRepository, serverId, log);
		throwIfServerDoesNotBelongToUser(serverToModify, user);
		if(serverModifications.getPort() != 0) serverToModify.setPort(serverModifications.getPort());
		if(serverModifications.getUrl() != null) serverToModify.setUrl(serverModifications.getUrl());
		serverRepository.save(serverToModify);
	}

	/**
	 * Deletes a server from the database
	 *
	 * @param serverId the id of the server to delete
	 * @throws ResourceNotFoundException if the server does not exist
	 */
	public void deleteServer(int serverId, String token) {
		log.info(String.format("Deleting server %s", serverId));
		User user = userService.findUserByToken(token).orElseThrow();
		Server serverToDelete = Utils.findObjectOrThrow(serverRepository, serverId, log);
		throwIfServerDoesNotBelongToUser(serverToDelete, user);
		serverRepository.delete(serverToDelete);
	}

	private void throwIfServerDoesNotBelongToUser(Server server, User user) {
		if(!server.getUser().equals(user)) throw new IllegalServerAccessException("Cannot access a server that is not yours !");
	}
}
