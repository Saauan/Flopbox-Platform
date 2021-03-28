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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import saauan.flopbox.AbstractIntegrationTest;
import saauan.flopbox.server.Server;

import java.util.Objects;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractFTPIntegrationTest extends AbstractIntegrationTest {
	public static final String HOST = "localhost";
	protected static final int PORT = 6667;
	public static final String TEST_TEXT_FILE_CONTENT = "abcdef 1234567890";
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
		fileSystem.add(new FileEntry("/home/text.txt", TEST_TEXT_FILE_CONTENT));
		fileSystem.add(new FileEntry("/home/run.exe"));
		fileSystem.add(new DirectoryEntry("/dev"));
		fakeFtpServer.setFileSystem(fileSystem);
		fakeFtpServer.setServerControlPort(PORT);

		fakeFtpServer.start();
	}

	protected MvcResult sendRequestToList(ResultMatcher expectedResponseCode, int serverId, String path)
			throws Exception {
		return sendRequestToFlopBox(expectedResponseCode,
				get(String.format("/servers/%s/list?path=%s", serverId, path)));
	}

	protected MvcResult downloadFile(ResultMatcher expectedResponseCode, int serverId, String path) throws Exception {
		return sendRequestToFlopBox(expectedResponseCode,
				MockMvcRequestBuilders.get(String.format("/servers/%s/files?path=%s&binary=false", serverId, path)));
	}

	protected MvcResult downloadBinaryFile(ResultMatcher expectedResponseCode, int serverId, String path)
			throws Exception {
		return sendRequestToFlopBox(expectedResponseCode,
				MockMvcRequestBuilders.get(String.format("/servers/%s/files?path=%s&binary=true", serverId, path)));
	}

	protected MvcResult downloadFiles(ResultMatcher expectedResponseCode, int serverId, String path) throws Exception {
		return sendRequestToFlopBox(expectedResponseCode,
				MockMvcRequestBuilders.get(String.format("/servers/%s/directories?path=%s", serverId, path)));
	}

	protected void uploadFileToServer(ResultMatcher expectedResponseCode, int serverId, String path,
									  MockMultipartFile jsonFile) throws Exception {
		sendRequestToFlopBox(expectedResponseCode, MockMvcRequestBuilders
				.multipart(String.format("/servers/%s/files?path=%s&binary=false", serverId, path))
				.file(jsonFile)
				.contentType(Objects.requireNonNull(jsonFile.getContentType())));
	}

	protected void uploadFilesToServer(ResultMatcher expectedResponseCode, int serverId, String path1,
									   String path2,
									   MockMultipartFile jsonFile, MockMultipartFile jsonFile2, boolean binary1,
									   boolean binary2) throws Exception {
		sendRequestToFlopBox(expectedResponseCode, MockMvcRequestBuilders
				.multipart(
						String.format("/servers/%s/files?path=%s&path=%s&binary=%s&binary=%s", serverId, path1, path2,
								binary1, binary2))
				.file(jsonFile).file(jsonFile2)
				.contentType(Objects.requireNonNull(jsonFile.getContentType())));
	}

	protected void uploadBinaryFileToServer(ResultMatcher expectedResponseCode, int serverId, String path,
											MockMultipartFile jsonFile) throws Exception {
		sendRequestToFlopBox(expectedResponseCode, MockMvcRequestBuilders
				.multipart(String.format("/servers/%s/files?path=%s&binary=true", serverId, path))
				.file(jsonFile)
				.contentType(Objects.requireNonNull(jsonFile.getContentType())));
	}

	protected MvcResult uploadBinaryFilesToServer(ResultMatcher expectedResponseCode, int serverId, String path1,
												  String path2,
												  MockMultipartFile jsonFile, MockMultipartFile jsonFile2)
			throws Exception {
		return sendRequestToFlopBox(expectedResponseCode, MockMvcRequestBuilders
				.multipart(String.format("/servers/%s/files?path=%s&path=%s&binary=true&binary=false", serverId, path1,
						path2))
				.file(jsonFile).file(jsonFile2)
				.contentType(Objects.requireNonNull(jsonFile.getContentType())));
	}

	protected void sendRequestToRenameFile(ResultMatcher expectedResponseCode, int serverId, String path,
										   String to)
			throws Exception {
		sendRequestToFlopBox(expectedResponseCode,
				patch(String.format("/servers/%s/files?path=%s&to=%s", serverId, path, to)));
	}

	protected void sendRequestToDeleteFile(ResultMatcher expectedResponseCode, int serverId, String path)
			throws Exception {
		sendRequestToFlopBox(expectedResponseCode,
				delete(String.format("/servers/%s/files?path=%s&", serverId, path)));
	}

	protected void sendRequestToCreateDirectory(ResultMatcher expectedResponseCode, int serverId, String path)
			throws Exception {
		sendRequestToFlopBox(expectedResponseCode,
				post(String.format("/servers/%s/directories?path=%s", serverId, path)));

	}

	protected void sendRequestToDeleteDirectory(ResultMatcher expectedResponseCode, int serverId, String path)
			throws Exception {
		sendRequestToFlopBox(expectedResponseCode,
				delete(String.format("/servers/%s/directories?path=%s", serverId, path)));
	}

	protected void sendRequestToRenameDirectory(ResultMatcher expectedResponseCode, int serverId, String path,
												String to)
			throws Exception {
		sendRequestToFlopBox(expectedResponseCode,
				patch(String.format("/servers/%s/directories?path=%s&to=%s", serverId, path, to)));
	}

	protected MvcResult sendRequestToFlopBox(ResultMatcher expectedResponseCode,
											 MockHttpServletRequestBuilder operation)
			throws Exception {
		return this.mockMvc.perform(operation.characterEncoding("utf-8")
				.header(HttpHeaders.AUTHORIZATION, BEARER + currentAuthToken)
				.header(FTPController.FTP_USERNAME, ftpUsername)
				.header(FTPController.FTP_PASSWORD, ftpPassword))
				.andExpect(expectedResponseCode)
				.andReturn();
	}

}
