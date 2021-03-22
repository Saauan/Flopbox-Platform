package saauan.flopbox.ftp;

import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import saauan.flopbox.server.Server;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@CommonsLog
public class FTPConnectorImpl implements FTPConnector {

	private FTPClient connectToServer(Server server, String username, String password) throws IOException {
		log.debug(String.format("Connecting to the FTP Server %s", server.getUrl()));
		FTPClient ftpClient = new FTPClient();
		ftpClient.connect(server.getUrl(), server.getPort());
		ftpClient.enterLocalPassiveMode();
		if (username != null && !username.isBlank()) {
			log.debug("Login in the server");
			if (!ftpClient.login(username, password)) {
				log.debug(String.format("Wrong creds %s | %s", username, password));
				throw new FTPOperationException(
						String.format("Wrong user or pass for server %s : %s", server, ftpClient.getReplyString()));
			}
		}
		return ftpClient;
	}

	@Override
	public List<FTPFile> list(Server server, String path, String username, String password) {
		log.info(String.format("Listing files from the FTP Server at path %s", path));
		try {
			FTPClient ftpClient = connectToServer(server, username, password);
			log.debug("Sending the list command");
			List<FTPFile> files = Arrays.asList(ftpClient.listFiles(path));
			return files;
		} catch (IOException e) {
			throw new FTPConnectException(String.format("Could not connect to %s", server), e);
		}
	}

	@Override
	public void sendFile(Server server, String path, String username, String password, MultipartFile file) {
		log.info(String.format("Sending a file to the FTP Server at path %s", path));
		try {
			FTPClient ftpClient = connectToServer(server, username, password);
			if (!ftpClient.storeFile(path, file.getInputStream())) {
				throw new FTPOperationException(ftpClient.getReplyString());
			}
		} catch (IOException e) {
			throw new FTPConnectException(String.format("Could not connect to %s", server), e);
		}
	}

	@Override
	public void getFile(Server server, String path, String username, String password) {

	}

	@Override
	public void createDirectory(Server server, String path, String username, String password) {
		log.info(String.format("Creating new directory with path %s", path));
		try {
			FTPClient ftpClient = connectToServer(server, username, password);
			if (!ftpClient.makeDirectory(path)) {
				throw new FTPOperationException(
						String.format("There was an error during the make directory : %s", ftpClient.getReplyString()));
			}
		} catch (IOException e) {
			throw new FTPConnectException(String.format("Could not connect to %s", server), e);
		}
	}

	@Override
	public void deleteDirectory(Server server, String path, String username, String password) {
		log.info(String.format("Deleting directory with path %s", path));
		try {
			FTPClient ftpClient = connectToServer(server, username, password);
			if (!ftpClient.removeDirectory(path)) {
				throw new FTPOperationException(
						String.format("There was an error during the delete directory : %s",
								ftpClient.getReplyString()));
			}
		} catch (IOException e) {
			throw new FTPConnectException(String.format("Could not connect to %s", server), e);
		}
	}

	@Override
	public void renameDirectory(Server server, String path, String to, String username, String password) {
		log.info(String.format("Deleting directory with path %s", path));
		try {
			FTPClient ftpClient = connectToServer(server, username, password);
			if (!ftpClient.rename(path, to)) {
				throw new FTPOperationException(
						String.format("There was an error during the delete directory : %s",
								ftpClient.getReplyString()));
			}
		} catch (IOException e) {
			throw new FTPConnectException(String.format("Could not connect to %s", server), e);
		}
	}

}
