package saauan.flopbox.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Component;
import saauan.flopbox.server.Server;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class FTPConnector {
	public List<FTPFile> list(Server server, String path, String username, String password) {
		try {
			FTPClient ftpClient = new FTPClient();
			ftpClient.connect(server.getUrl().toString(), server.getPort());
			if (username != null && !username.isBlank()) {
				if (!ftpClient.login(username, password)) {
					throw new FTPLoginException(String.format("Wrong user or pass for server %s", server));
				}
			}
			List<FTPFile> files = Arrays.asList(ftpClient.listFiles(path));
			return files;

		} catch (IOException e) {
			throw new FTPConnectException(String.format("Could not connect to %s", server));
		}
	}
}
