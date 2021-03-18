package saauan.flopbox.ftp;

import org.apache.commons.net.ftp.FTPFile;
import org.springframework.web.multipart.MultipartFile;
import saauan.flopbox.server.Server;

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
	 * @throws FTPConnectException   if there is an error while connecting to the server
	 * @throws FTPOperationException if there is an error during the operation
	 */
	void sendFile(Server server, String path, String username, String password, MultipartFile file);

	void getFile(Server server, String path, String username, String password);
}
