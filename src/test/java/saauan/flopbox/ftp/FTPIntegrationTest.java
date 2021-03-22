package saauan.flopbox.ftp;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import saauan.flopbox.CustomFTPFile;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class FTPIntegrationTest extends AbstractFTPIntegrationTest {

	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void cannotConnectToFTPServerIfCredentialsAreWrong() throws Exception {
		ftpPassword = "bla";
		sendRequestToList(status().isForbidden(), ftpServerPOJO.getId(), "/home");
	}

	@Test
	public void canListIfPathDoesNotExist() throws Exception {
		sendRequestToList(status().isOk(), ftpServerPOJO.getId(), "/home/bla");
	}

	@Test
	public void listReturnsACorrectListOfFiles() throws Exception {
		MvcResult result = sendRequestToList(status().isOk(), ftpServerPOJO.getId(), "/home");
		System.out.println(result.getResponse().getContentAsString());
		List<CustomFTPFile> files = objectMapper
				.readValue(objectMapper.readTree(result.getResponse().getContentAsString()).get("files").toString(),
						new TypeReference<List<CustomFTPFile>>() {
						});
		System.err.println(files.get(0).toFormattedString());
		Assertions.assertFalse(files.isEmpty());
		Assertions.assertEquals(fileSystem.listFiles("/home").size(), files.size());
	}

	@Test
	public void sendFileStoresItOnTheFTPServer() throws Exception {
		MockMultipartFile jsonFile = new MockMultipartFile("file", "test.json", MediaType.APPLICATION_JSON.toString(),
				"{\"key1\": \"value1\"}".getBytes());
		MvcResult result = uploadFileToServer(status().isOk(), ftpServerPOJO.getId(), "/home/test.json", jsonFile);
		assert fakeFtpServer.getFileSystem().isFile("/home/text.txt");
		Assertions.assertTrue(fakeFtpServer.getFileSystem().isFile("/home/test.json"));
		System.err.println(result.getResponse().getContentAsString());
	}

	@Test
	public void canSendMultipleFiles() throws Exception {
		MockMultipartFile jsonFile = new MockMultipartFile("file", "test.json", MediaType.APPLICATION_JSON.toString(),
				"{\"key1\": \"value1\"}".getBytes());
		MockMultipartFile jsonFile2 = new MockMultipartFile("file", "test2.json", MediaType.APPLICATION_JSON.toString(),
				"{\"key2\": \"value2\"}".getBytes());

		MvcResult result = uploadFilesToServer(status().isOk(), ftpServerPOJO.getId(), "/home/test.json",
				"/home/test2.json", jsonFile, jsonFile2);
		Assertions.assertTrue(fakeFtpServer.getFileSystem().isFile("/home/test.json"));
		Assertions.assertTrue(fakeFtpServer.getFileSystem().isFile("/home/test2.json"));
		System.err.println(result.getResponse().getContentAsString());
	}

	@Test
	public void cannotSendFileToUnknownPath() throws Exception {
		MockMultipartFile jsonFile = new MockMultipartFile("file", "test.json", MediaType.APPLICATION_JSON.toString(),
				"{\"key1\": \"value1\"}".getBytes());
		uploadFileToServer(status().isForbidden(), ftpServerPOJO.getId(), "/data/test.json", jsonFile);
	}

	@Test
	public void canCreateDirectory() throws Exception {
		sendRequestToCreateDirectory(status().isOk(), ftpServerPOJO.getId(), "/home/myDir");

		fakeFtpServer.getFileSystem().isDirectory("/home/myDir");
	}

	@Test
	public void cannotCreateDirectoryIfPathIncorrect() throws Exception {
		sendRequestToCreateDirectory(status().isForbidden(), ftpServerPOJO.getId(), "/data/yo");
		sendRequestToCreateDirectory(status().isForbidden(), ftpServerPOJO.getId(), "/home/myDir/bloup");

	}

	@Test
	public void downloadFileDownloadsAFileFromTheFtpServer() throws Exception {
		Assertions.fail();
	}

	@Test
	public void canUploadAndDownloadABinaryFile() throws Exception {
		Assertions.fail();
	}

	@Test
	public void canUploadAndDownloadATextFile() throws Exception {
		Assertions.fail();
	}


}
