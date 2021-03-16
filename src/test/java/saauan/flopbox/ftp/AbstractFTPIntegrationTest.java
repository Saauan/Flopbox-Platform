package saauan.flopbox.ftp;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import saauan.flopbox.AbstractIntegrationTest;
import saauan.flopbox.server.Server;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractFTPIntegrationTest extends AbstractIntegrationTest {
	public static final String HOST = "localhost";
	protected static final int PORT = 6667;
	public Server ftpServerPOJO;
	public String ftpUsername = "anonymous";
	public String ftpPassword = "anonymous";
	public String homeDirectory = "/home";
	public FileSystem fileSystem;
	FakeFtpServer fakeFtpServer = new FakeFtpServer();

	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		authAndChangeUser(authUser1);
		if (ftpServerPOJO == null) {
			Server ftpServer = new Server(HOST, PORT);
			ftpServerPOJO = objectMapper.readValue(objectMapper
					.readTree(sendRequestToCreateServer(status().isCreated(), ftpServer).getResponse()
							.getContentAsString())
					.get("server").toString(), new TypeReference<>() {
			});
		}
		setUpFakeFTPServer();
	}

	@AfterEach
	public void tearDown() {
		fakeFtpServer.stop();
	}

	private void setUpFakeFTPServer() {
		fakeFtpServer.addUserAccount(new UserAccount(ftpUsername, ftpPassword, homeDirectory));

		fileSystem = new UnixFakeFileSystem();
		fileSystem.add(new DirectoryEntry("/home"));
		fileSystem.add(new FileEntry("/home/text.txt", "abcdef 1234567890"));
		fileSystem.add(new FileEntry("/home/run.exe"));
		fileSystem.add(new DirectoryEntry("/dev"));
		fakeFtpServer.setFileSystem(fileSystem);
		fakeFtpServer.setServerControlPort(PORT);

		fakeFtpServer.start();
	}

	protected MvcResult sendRequestToList(ResultMatcher expectedResponseCode, int serverId, String path)
			throws Exception {
		return this.mockMvc.perform(get(String.format("/servers/%s/files/list?path=%s", serverId, path))
				.characterEncoding("utf-8")
				.header(HttpHeaders.AUTHORIZATION, BEARER + currentAuthToken)
				.header(FTPController.FTP_USERNAME, ftpUsername)
				.header(FTPController.FTP_PASSWORD, ftpPassword))
				.andExpect(expectedResponseCode)
				.andReturn();
	}

}
