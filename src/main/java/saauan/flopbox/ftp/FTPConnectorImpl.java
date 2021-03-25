package saauan.flopbox.ftp;

import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import saauan.flopbox.server.Server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

@Component
@CommonsLog
public class FTPConnectorImpl implements FTPConnector {

	@Override
	public List<FTPFile> list(Server server, String path, String username, String password) {
		log.info(String.format("Listing files from the FTP Server at path %s", path));
		return sendCommandWithReturn(server, username, password,
				(FTPClient ftpClient) -> Arrays.asList(ftpClient.listFiles(path)));
	}

	@Override
	public void sendFile(Server server, String path, String username, String password, MultipartFile file) {
		log.info(String.format("Sending a file to the FTP Server at path %s", path));
		sendSimpleCommand(server, username, password,
				(FTPClient ftpClient) -> ftpClient.storeFile(path, file.getInputStream()));
	}

	@Override
	public void getFile(Server server, String path, String username, String password, OutputStream out) {
		log.info(String.format("Download file %s", path));
		sendSimpleCommand(server, username, password,
				(FTPClient ftpClient) -> ftpClient.retrieveFile(path, out));
	}

	@Override
	public void renameFile(Server server, String path, String to, String username, String password) {
		log.info(String.format("Renaming file from %s to %s", path, to));
		renameResource(server, path, to, username, password);
	}

	@Override
	public void deleteFile(Server server, String path, String username, String password) {
		log.info(String.format("Deleting file %s", path));
		sendSimpleCommand(server, username, password, (FTPClient ftpClient) -> ftpClient.deleteFile(path));
	}

	@Override
	public void createDirectory(Server server, String path, String username, String password) {
		log.info(String.format("Creating new directory with path %s", path));
		sendSimpleCommand(server, username, password, (FTPClient ftpClient) -> ftpClient.makeDirectory(path));
	}

	@Override
	public void deleteDirectory(Server server, String path, String username, String password) {
		log.info(String.format("Deleting directory with path %s", path));
		sendSimpleCommand(server, username, password, (FTPClient ftpClient) -> ftpClient.removeDirectory(path));
	}

	@Override
	public void renameDirectory(Server server, String path, String to, String username, String password) {
		log.info(String.format("Renaming directory with path %s to %s", path, to));
		renameResource(server, path, to, username, password);
	}

	private void renameResource(Server server, String path, String to, String username, String password) {
		sendSimpleCommand(server, username, password, (FTPClient ftpClient) -> ftpClient.rename(path, to));
	}

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

	private void sendSimpleCommand(Server server, String username, String password, SimpleCommand operation) {
		try {
			FTPClient ftpClient = connectToServer(server, username, password);
			if (!operation.execute(ftpClient)) {
				safeDisconnect(ftpClient);
				throw new FTPOperationException(
						String.format("There was an error while performing the operation : %s",
								ftpClient.getReplyString()));
			}
			safeDisconnect(ftpClient);
		} catch (IOException e) {
			throw new FTPConnectException(String.format("Could not connect to %s", server), e);
		}
	}

	private <T> T sendCommandWithReturn(Server server, String username, String password,
										CommandWithReturn<T> operation) {
		try {
			FTPClient ftpClient = connectToServer(server, username, password);
			T data = operation.execute(ftpClient);
			safeDisconnect(ftpClient);
			return data;
		} catch (IOException e) {
			throw new FTPConnectException(String.format("Could not connect to %s", server), e);
		}
	}

	private void safeDisconnect(FTPClient client) {
		try {
			client.logout();
		} catch (IOException ex) {
			log.error("Unable to send QUIT command", ex);
		} finally {
			try {
				client.disconnect();
			} catch (IOException ex) {
				log.error("Unable to disconnect the client", ex);
			}
		}
	}

	protected interface SimpleCommand {
		boolean execute(FTPClient ftpClient) throws IOException;
	}

	protected interface CommandWithReturn<T> {
		T execute(FTPClient ftpClient) throws IOException;
	}

}
