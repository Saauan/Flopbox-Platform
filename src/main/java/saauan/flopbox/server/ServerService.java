package saauan.flopbox.server;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import saauan.flopbox.Utils;

import java.util.List;

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
	 * @throws ServerAlreadyExistException if the server already exists
	 */
	public void addServer(Server server) {
		log.info(String.format("Adding server %s", server));
		if (this.serverRepository.findByUrl(server.getUrl()) != null) {
			Utils.logAndThrow(log, ServerAlreadyExistException.class, String.format("Server %s already exists", server.getUrl()));
		}
		serverRepository.save(server);
	}

	/**
	 * Returns a list of all the servers
	 *
	 * @return Returns a list of all the servers
	 */
	public List<Server> getAllServers() {
		return serverRepository.findAll();
	}
}
