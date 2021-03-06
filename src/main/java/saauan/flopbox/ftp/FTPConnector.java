package saauan.flopbox.ftp;

import org.apache.commons.net.ftp.FTPFile;
import org.springframework.web.multipart.MultipartFile;
import saauan.flopbox.server.Server;

import java.io.OutputStream;
import java.util.List;

/**
 * Connects to a FTP server and sends commands to it
 */
public interface FTPConnector {
	/**
	 * Sends the LIST command and return a list of FTP Files resulting from the list
	 *
	 * @param server   the server to connect to
	 * @param path     the path of the list
	 * @param username the username of the userr
	 * @param password the password of the user
	 * @return a list of FTP Files
	 * @throws FTPConnectException   if there is an error while connecting to the server
	 * @throws FTPOperationException if there is an error during the operation
	 */
	List<FTPFile> list(Server server, String path, String username, String password);

	/**
	 * Uploads a file to the ftp server
	 *
	 * @param server   the server to connect to
	 * @param path     the path where the file will be stored
	 * @param username the username of the user
	 * @param password the password of the user
	 * @param file     file the file to upload
	 * @param fileType the type of the file to send
	 * @throws FTPConnectException   if there is an error while connecting to the server
	 * @throws FTPOperationException if there is an error during the operation
	 */
	void sendFile(Server server, String path, String username, String password, MultipartFile file,
				  FileType fileType);

	/**
	 * Downloads a file from the server
	 *
	 * @param server   the server to connect to
	 * @param path     the path where the file will be stored
	 * @param username the username of the user
	 * @param password the password of the user
	 * @param fileType the type of the file to download
	 * @throws FTPConnectException   if there is an error while connecting to the server
	 * @throws FTPOperationException if there is an error during the operation
	 */
	void downloadFile(Server server, String path, String username, String password, OutputStream out,
					  FileType fileType);

	/**
	 * Renames a file
	 *
	 * @param server   the server to connect to
	 * @param path     the path of the file
	 * @param to       the new path of the file
	 * @param username the username of the user
	 * @param password the password of the user
	 * @throws FTPConnectException   if there is an error while connecting to the server
	 * @throws FTPOperationException if there is an error during the operation
	 */
	void renameFile(Server server, String path, String to, String username, String password);

	/**
	 * Deletes a file
	 *
	 * @param server   the server to connect to
	 * @param path     the path of the file
	 * @param username the username of the user
	 * @param password the password of the user
	 * @throws FTPConnectException   if there is an error while connecting to the server
	 * @throws FTPOperationException if there is an error during the operation
	 */
	void deleteFile(Server server, String path, String username, String password);

	/**
	 * Creates a new directory on the server
	 *
	 * @param server   the server to connect to
	 * @param path     the path of the directory to create
	 * @param username the username of the user
	 * @param password the password of the user
	 * @throws FTPConnectException   if there is an error while connecting to the server
	 * @throws FTPOperationException if there is an error during the operation
	 */
	void createDirectory(Server server, String path, String username, String password);

	/**
	 * Deletes a directory on the server
	 *
	 * @param server   the server to connect to
	 * @param path     the path of the directory to delete
	 * @param username the username of the user
	 * @param password the password of the user
	 * @throws FTPConnectException   if there is an error while connecting to the server
	 * @throws FTPOperationException if there is an error during the operation
	 */
	void deleteDirectory(Server server, String path, String username, String password);

	/**
	 * Renames a directory
	 *
	 * @param server   the server to connect to
	 * @param path     the path of the directory
	 * @param to        the new path of the directory
	 * @param username the username of the user
	 * @param password the password of the user
	 * @throws FTPConnectException   if there is an error while connecting to the server
	 * @throws FTPOperationException if there is an error during the operation
	 */
	void renameDirectory(Server server, String path, String to, String username, String password);
}
