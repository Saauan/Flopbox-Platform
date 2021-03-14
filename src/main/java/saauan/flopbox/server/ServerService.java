package saauan.flopbox.server;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import saauan.flopbox.Utils;

@Service
@CommonsLog
public class ServerService {

	private final ServerRepository serverRepository;

	@Autowired
	public ServerService(ServerRepository serverRepository) {
		this.serverRepository = serverRepository;
	}

	public void addServer(Server server) {
		log.info(String.format("Adding server %s", server));
		if (this.serverRepository.findByUrl(server.getUrl()) != null) {
			Utils.logAndThrow(log, ServerAlreadyExistException.class, String.format("Server %s already exists", server.getUrl()));
		}
		serverRepository.save(server);
	}
}
