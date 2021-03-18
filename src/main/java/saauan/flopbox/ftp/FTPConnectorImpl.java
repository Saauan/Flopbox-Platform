package saauan.flopbox.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import saauan.flopbox.server.Server;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class FTPConnectorImpl implements FTPConnector {

	private FTPClient connectToServer(Server server, String username, String password) throws IOException {
		FTPClient ftpClient = new FTPClient();
		ftpClient.connect(server.getUrl(), server.getPort());
		if (username != null && !username.isBlank()) {
			if (!ftpClient.login(username, password)) {
				throw new FTPOperationException(String.format("Wrong user or pass for server %s", server));
			}
		}
		return ftpClient;
	}

	@Override
	public List<FTPFile> list(Server server, String path, String username, String password) {
		try {
			FTPClient ftpClient = connectToServer(server, username, password);
			return Arrays.asList(ftpClient.listFiles(path));
		} catch (IOException e) {
			throw new FTPConnectException(String.format("Could not connect to %s", server), e);
		}
	}

	@Override
	public void sendFile(Server server, String path, String username, String password, MultipartFile file) {
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

}
