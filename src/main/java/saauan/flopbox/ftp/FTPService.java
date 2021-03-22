package saauan.flopbox.ftp;

import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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

	/**
	 * Returns a list of files contained in the path, on the FTP server.
	 *
	 * @param serverId the id of the server
	 * @param token    the authentication token of the user
	 * @param path     the path where the file will be stored
	 * @param username the FTP username of the user
	 * @param password the FTP password of the user
	 * @param file     the file to upload
	 * @throws FTPConnectException   if there is an error while connecting to the server
	 * @throws FTPOperationException if there is an error while performing the operation on the server
	 */
	public void store(int serverId, String token, String path, String username, String password, MultipartFile file) {
		User user = userRepository.findByToken(token).orElseThrow();
		Server server = serverRepository.findByIdAndUser(serverId, user)
				.orElseThrow(
						() -> new ResourceNotFoundException(String.format("The server %s was not found", serverId)));
		ftpConnector.sendFile(server, path, username, password, file);
	}

	public Resource loadAsResource(int serverId, String token, String path, String headerValue, String headerValue1) {
		return null;
	}

	/**
	 * Creates a new directory on the server
	 *
	 * @param serverId the id of the server
	 * @param token    the authentication token of the user
	 * @param path     the path where the new directory will be
	 * @param username the FTP username of the user
	 * @param password the FTP password of the user
	 * @throws FTPConnectException   if there is an error while connecting to the server
	 * @throws FTPOperationException if there is an error while performing the operation on the server
	 */
	public void createDirectory(int serverId, String token, String path, String username, String password) {
		User user = userRepository.findByToken(token).orElseThrow();
		Server server = serverRepository.findByIdAndUser(serverId, user)
				.orElseThrow(
						() -> new ResourceNotFoundException(String.format("The server %s was not found", serverId)));
		ftpConnector.createDirectory(server, path, username, password);
	}

	/**
	 * Deletes a directory
	 *
	 * @param serverId the id of the server
	 * @param token    the authentication token of the user
	 * @param path     the path of the directory
	 * @param username the FTP username of the user
	 * @param password the FTP password of the user
	 * @throws FTPConnectException   if there is an error while connecting to the server
	 * @throws FTPOperationException if there is an error while performing the operation on the server
	 */
	public void deleteDirectory(int serverId, String token, String path, String username, String password) {
		User user = userRepository.findByToken(token).orElseThrow();
		Server server = serverRepository.findByIdAndUser(serverId, user)
				.orElseThrow(
						() -> new ResourceNotFoundException(String.format("The server %s was not found", serverId)));
		ftpConnector.deleteDirectory(server, path, username, password);
	}

	/**
	 * Rename a directory
	 *
	 * @param serverId the id of the server
	 * @param token    the authentication token of the user
	 * @param path     the path of the directory
	 * @param to       the path to rename to
	 * @param username the FTP username of the user
	 * @param password the FTP password of the user
	 * @throws FTPConnectException   if there is an error while connecting to the server
	 * @throws FTPOperationException if there is an error while performing the operation on the server
	 */
	public void renameDirectory(int serverId, String token, String path, String to, String username, String password) {
		User user = userRepository.findByToken(token).orElseThrow();
		Server server = serverRepository.findByIdAndUser(serverId, user)
				.orElseThrow(
						() -> new ResourceNotFoundException(String.format("The server %s was not found", serverId)));
		ftpConnector.renameDirectory(server, path, to, username, password);
	}
}
