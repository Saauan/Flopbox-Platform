package saauan.flopbox.ftp;

import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import saauan.flopbox.server.Server;
import saauan.flopbox.server.ServerRepository;
import saauan.flopbox.user.User;
import saauan.flopbox.user.UserRepository;

import java.util.List;

@Service
public class FTPService {

	private final ServerRepository serverRepository;
	private final UserRepository userRepository;
	private final FTPConnector ftpConnector;

	@Autowired
	public FTPService(ServerRepository serverRepository, UserRepository userRepository,
					  FTPConnector ftpConnector) {
		this.serverRepository = serverRepository;
		this.userRepository = userRepository;
		this.ftpConnector = ftpConnector;
	}

	/**
	 * Returns a list of files contained in the path, on the FTP server.
	 *
	 * @param serverId the id of the server
	 * @param token    the authentication token of the user
	 * @param path     the path where to perform the list
	 * @param username the FTP username of the user
	 * @param password the FTP password of the user
	 * @return a list of FTP files
	 * @throws FTPConnectException   if there is an error while connecting to the server
	 * @throws FTPOperationException if there is an error while performing the operation on the server
	 */
	public List<FTPFile> list(int serverId, String token, String path, String username, String password) {
		User user = userRepository.findByToken(token).orElseThrow();
		Server server = serverRepository.findByIdAndUser(serverId, user)
				.orElseThrow(
						() -> new ResourceNotFoundException(String.format("The server %s was not found", serverId)));
		return ftpConnector.list(server, path, username, password);
	}
}
