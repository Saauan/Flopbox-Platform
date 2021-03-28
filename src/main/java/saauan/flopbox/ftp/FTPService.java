package saauan.flopbox.ftp;

import lombok.SneakyThrows;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import saauan.flopbox.Utils;
import saauan.flopbox.server.Server;
import saauan.flopbox.server.ServerRepository;
import saauan.flopbox.user.User;
import saauan.flopbox.user.UserRepository;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Provides operations for interacting with an ftp server
 */
@Service
@CommonsLog
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
		User user = Utils.findObjectOrThrow(() -> userRepository.findByToken(token), log);
		Server server = Utils.findObjectOrThrow(() -> serverRepository.findByIdAndUser(serverId, user), log);
		return ftpConnector.list(server, path, username, password);
	}

	/**
	 * Stores a file on the ftp server
	 *
	 * @param serverId the id of the server
	 * @param token    the authentication token of the user
	 * @param path     the path where the file will be stored
	 * @param username the FTP username of the user
	 * @param password the FTP password of the user
	 * @param file     the file to upload
	 * @param binary   true if the file is of binary type, false otherwise
	 * @throws FTPConnectException   if there is an error while connecting to the server
	 * @throws FTPOperationException if there is an error while performing the operation on the server
	 */
	public void store(int serverId, String token, String path, String username, String password, MultipartFile file,
					  boolean binary) {
		User user = Utils.findObjectOrThrow(() -> userRepository.findByToken(token), log);
		Server server = Utils.findObjectOrThrow(() -> serverRepository.findByIdAndUser(serverId, user), log);
		ftpConnector.sendFile(server, path, username, password, file, getFileType(binary));
	}

	/**
	 * Downloads a file from the FTP server
	 *
	 * @param serverId the id of the server
	 * @param token    the authentication token of the user
	 * @param path     the path where the file is located
	 * @param username the FTP username of the user
	 * @param password the FTP password of the user
	 * @param binary   true if the file is of binary type, false otherwise
	 * @throws FTPConnectException   if there is an error while connecting to the server
	 * @throws FTPOperationException if there is an error while performing the operation on the server
	 */
	@SneakyThrows
	public Resource downloadFile(int serverId, String token, String path, String username, String password,
								 boolean binary) {
		User user = Utils.findObjectOrThrow(() -> userRepository.findByToken(token), log);
		Server server = Utils.findObjectOrThrow(() -> serverRepository.findByIdAndUser(serverId, user), log);
		File tempFile = getTemporaryFile(Path.of(path).getFileName().toString());
		OutputStream out = new FileOutputStream(tempFile);

		ftpConnector.downloadFile(server, path, username, password, out, getFileType(binary));

		out.close();
		return new FileSystemResource(tempFile);
	}

	private File getTemporaryFile(String fileName) {
		String tmpdir = System.getProperty("java.io.tmpdir");
		log.debug(String.format("Creating file %s", tmpdir + "/" + fileName));
		var file = new File(tmpdir + "/" + fileName);
		try {
			if (!file.exists() && !file.createNewFile()) {
				throw new RuntimeException("There was an error when creating the temporary file");
			}
		} catch (IOException e) {
			throw new RuntimeException("There was an error when creating the temporary file :", e);
		}
		return file;
	}

	private FileType getFileType(boolean binary) {
		FileType transferFileType = binary ? FileType.BINARY : FileType.ASCII;
		return transferFileType;
	}

	/**
	 * Rename a file
	 *
	 * @param serverId the id of the server
	 * @param token    the authentication token of the user
	 * @param path     the path of the file
	 * @param to       the path to rename to
	 * @param username the FTP username of the user
	 * @param password the FTP password of the user
	 * @throws FTPConnectException   if there is an error while connecting to the server
	 * @throws FTPOperationException if there is an error while performing the operation on the server
	 */
	public void renameFile(int serverId, String token, String path, String to, String username, String password) {
		User user = Utils.findObjectOrThrow(() -> userRepository.findByToken(token), log);
		Server server = Utils.findObjectOrThrow(() -> serverRepository.findByIdAndUser(serverId, user), log);
		ftpConnector.renameFile(server, path, to, username, password);
	}

	/**
	 * Delete a file
	 *
	 * @param serverId the id of the server
	 * @param token    the authentication token of the user
	 * @param path     the path of the file
	 * @param username the FTP username of the user
	 * @param password the FTP password of the user
	 * @throws FTPConnectException   if there is an error while connecting to the server
	 * @throws FTPOperationException if there is an error while performing the operation on the server
	 */
	public void deleteFile(int serverId, String token, String path, String username, String password) {
		User user = Utils.findObjectOrThrow(() -> userRepository.findByToken(token), log);
		Server server = Utils.findObjectOrThrow(() -> serverRepository.findByIdAndUser(serverId, user), log);
		ftpConnector.deleteFile(server, path, username, password);
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
		User user = Utils.findObjectOrThrow(() -> userRepository.findByToken(token), log);
		Server server = Utils.findObjectOrThrow(() -> serverRepository.findByIdAndUser(serverId, user), log);
		ftpConnector.createDirectory(server, path, username, password);
	}

	/**
	 * Downloads a directory in the form of a zip file
	 *
	 * @param serverId the id of the server
	 * @param token    the authentication token of the user
	 * @param path     the path where the directory is
	 * @param username the FTP username of the user
	 * @param password the FTP password of the user
	 * @return a zip resource containing the requested files
	 * @throws FTPConnectException   if there is an error while connecting to the server
	 * @throws FTPOperationException if there is an error while performing the operation on the server
	 */
	@SneakyThrows
	public Resource downloadDirectory(int serverId, String token, String path, String username, String password) {
		User user = Utils.findObjectOrThrow(() -> userRepository.findByToken(token), log);
		Server server = Utils.findObjectOrThrow(() -> serverRepository.findByIdAndUser(serverId, user), log);
		List<FTPFile> files = ftpConnector.list(server, path, username, password);
		File zipFile = getTemporaryFile("ftpZip.zip");
		FileOutputStream fos = new FileOutputStream(zipFile);
		ZipOutputStream zipOut = new ZipOutputStream(fos);
		for (FTPFile file : files) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ftpConnector.downloadFile(server, file.getName(), username, password, bos, FileType.BINARY);
			ZipEntry zipEntry = new ZipEntry(file.getName());
			zipOut.putNextEntry(zipEntry);
			zipOut.write(bos.toByteArray());
			bos.close();
		}
		zipOut.close();
		fos.close();
		return new FileSystemResource(zipFile);
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
		User user = Utils.findObjectOrThrow(() -> userRepository.findByToken(token), log);
		Server server = Utils.findObjectOrThrow(() -> serverRepository.findByIdAndUser(serverId, user), log);
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
		User user = Utils.findObjectOrThrow(() -> userRepository.findByToken(token), log);
		Server server = Utils.findObjectOrThrow(() -> serverRepository.findByIdAndUser(serverId, user), log);
		ftpConnector.renameDirectory(server, path, to, username, password);
	}
}
