package saauan.flopbox.ftp;

import org.apache.commons.net.ftp.FTPFile;
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
}
