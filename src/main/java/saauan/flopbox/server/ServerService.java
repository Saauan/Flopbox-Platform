package saauan.flopbox.server;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import saauan.flopbox.Utils;
import saauan.flopbox.exceptions.ResourceAlreadyExistException;
import saauan.flopbox.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
@CommonsLog
public class ServerService {

	private final ServerRepository serverRepository;

	@Autowired
	public ServerService(ServerRepository serverRepository) {
		this.serverRepository = serverRepository;
	}

	/**
	 * Adds a server to the database
	 *
	 * @param server the server to add
	 * @throws ResourceAlreadyExistException if the server already exists
	 */
	public void addServer(Server server) {
		log.info(String.format("Adding server %s", server));
		if (this.serverRepository.findByUrl(server.getUrl()) != null) {
			Utils.logAndThrow(log, ResourceAlreadyExistException.class, String.format("Server %s already exists", server.getUrl()));
		}
		serverRepository.save(server);
	}

	/**
	 * Returns a list of all the servers
	 *
	 * @return Returns a list of all the servers
	 */
	public List<Server> getAllServers() {
		log.info("Getting all servers");
		return serverRepository.findAll();
	}

	/**
	 * Gets the server from the database
	 *
	 * @param serverId the id of the server
	 * @return the server corresponding to `serverId`
	 * @throws ResourceNotFoundException if the server does not exist
	 */
	public Server getServer(int serverId) {
		log.info(String.format("Getting server %s", serverId));
		return findServerOrThrow(serverId);
	}

	private Server findServerOrThrow(int serverId) {
		Optional<Server> server = serverRepository.findById(serverId);
		if(server.isEmpty()) {
			Utils.logAndThrow(log, ResourceNotFoundException.class, String.format("server %d was not found", serverId));
			assert false: "Should never arrive here";
		}
		return server.get();
	}

	/**
	 * Modifies a server in the database
	 *
	 * @param serverModifications a server object containing changes to apply to the server
	 * @param serverId the id of the server to modify
	 * @throws ResourceNotFoundException if the server does not exist
	 */
	public void modifyServer(Server serverModifications, int serverId) {
		log.info(String.format("Modifying server %s", serverId));
		Server serverToModify = findServerOrThrow(serverId);
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
	public void deleteServer(int serverId) {
		log.info(String.format("Deleting server %s", serverId));
		Server serverToDelete = findServerOrThrow(serverId);
		serverRepository.delete(serverToDelete);
	}
}
