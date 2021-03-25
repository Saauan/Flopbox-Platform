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
						new TypeReference<>() {
						});
		System.err.println(files.get(0).toFormattedString());
		Assertions.assertFalse(files.isEmpty());
		Assertions.assertEquals(fileSystem.listFiles("/home").size(), files.size());
	}

	@Test
	public void sendFileStoresItOnTheFTPServer() throws Exception {
		MockMultipartFile jsonFile = new MockMultipartFile("file", "test.json", MediaType.APPLICATION_JSON.toString(),
				"{\"key1\": \"value1\"}".getBytes());

		uploadFileToServer(status().isOk(), ftpServerPOJO.getId(), "/home/test.json", jsonFile);

		assert fakeFtpServer.getFileSystem().isFile("/home/text.txt");
		Assertions.assertTrue(fakeFtpServer.getFileSystem().isFile("/home/test.json"));
	}

	@Test
	public void canSendMultipleFiles() throws Exception {
		MockMultipartFile jsonFile = new MockMultipartFile("file", "test.json", MediaType.APPLICATION_JSON.toString(),
				"{\"key1\": \"value1\"}".getBytes());
		MockMultipartFile jsonFile2 = new MockMultipartFile("file", "test2.json", MediaType.APPLICATION_JSON.toString(),
				"{\"key2\": \"value2\"}".getBytes());

		uploadFilesToServer(status().isOk(), ftpServerPOJO.getId(), "/home/test.json",
				"/home/test2.json", jsonFile, jsonFile2, false, false);

		Assertions.assertTrue(fakeFtpServer.getFileSystem().isFile("/home/test.json"));
		Assertions.assertTrue(fakeFtpServer.getFileSystem().isFile("/home/test2.json"));
	}

	@Test
	public void cannotSendFileToUnknownPath() throws Exception {
		MockMultipartFile jsonFile = new MockMultipartFile("file", "test.json", MediaType.APPLICATION_JSON.toString(),
				"{\"key1\": \"value1\"}".getBytes());

		uploadFileToServer(status().isForbidden(), ftpServerPOJO.getId(), "/data/test.json", jsonFile);
	}

	@Test
	public void downloadFileDownloadsAFileFromTheFtpServer() throws Exception {
		Assertions.assertTrue(fakeFtpServer.getFileSystem().isFile("/home/text.txt"));

		MvcResult result = downloadFile(status().isOk(), ftpServerPOJO.getId(), "/home/text.txt");

		Assertions.assertEquals(TEST_TEXT_FILE_CONTENT, result.getResponse().getContentAsString());
	}

	@Test
	public void canUploadAndDownloadABinaryFile() throws Exception {
		byte[] fileContent = getClass().getClassLoader().getResourceAsStream("chibi_doctor.jpg").readAllBytes();
		MockMultipartFile imgFile = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
				fileContent);

		uploadBinaryFileToServer(status().isOk(), ftpServerPOJO.getId(), "/home/test.jpg", imgFile);
		MvcResult result = downloadBinaryFile(status().isOk(), ftpServerPOJO.getId(), "/home/test.jpg");

		checkBinaryFilesAreEquals(fileContent, result.getResponse().getContentAsByteArray());
	}

	@Test
	public void canUploadAndDownloadATextFile() throws Exception {
		String fileContent = String.format("{\"key1\": \"value1\"}%sHelloWorld", System.lineSeparator());
		MockMultipartFile jsonFile = new MockMultipartFile("file", "test.json", MediaType.APPLICATION_JSON.toString(),
				fileContent.getBytes());

		uploadFileToServer(status().isOk(), ftpServerPOJO.getId(), "/home/test.json", jsonFile);

		MvcResult result = downloadFile(status().isOk(), ftpServerPOJO.getId(), "/home/test.json");
		Assertions.assertEquals(fileContent, result.getResponse().getContentAsString());
	}

	@Test
	public void canUploadAndDownloadATwoFilesOfDifferentType() throws Exception {
		byte[] imgFileContent = getClass().getClassLoader().getResourceAsStream("chibi_doctor.jpg").readAllBytes();
		MockMultipartFile imgFile = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
				imgFileContent);
		String txtFileContent = String.format("{\"key1\": \"value1\"}%sHelloWorld", System.lineSeparator());
		MockMultipartFile jsonFile = new MockMultipartFile("file", "test.json", MediaType.APPLICATION_JSON.toString(),
				txtFileContent.getBytes());

		uploadFilesToServer(status().isOk(), ftpServerPOJO.getId(), "/home/test.jpg", "/home/test.json", imgFile,
				jsonFile,
				true, false);

		MvcResult result = downloadBinaryFile(status().isOk(), ftpServerPOJO.getId(), "/home/test.jpg");
		checkBinaryFilesAreEquals(imgFileContent, result.getResponse().getContentAsByteArray());

		result = downloadFile(status().isOk(), ftpServerPOJO.getId(), "/home/test.json");
		Assertions.assertEquals(txtFileContent, result.getResponse().getContentAsString());
	}

	private void checkBinaryFilesAreEquals(byte[] fileContent, byte[] actualFileContent) {
		Assertions.assertEquals(fileContent.length, actualFileContent.length);
		for (int i = 0; i < fileContent.length; i++) {
			Assertions.assertEquals(fileContent[i], actualFileContent[i]);
		}
	}

	@Test
	public void canCreateDirectory() throws Exception {
		sendRequestToCreateDirectory(status().isOk(), ftpServerPOJO.getId(), "/home/myDir");

		Assertions.assertTrue(fakeFtpServer.getFileSystem().isDirectory("/home/myDir"));
	}

	@Test
	public void cannotCreateDirectoryIfPathIncorrect() throws Exception {
		sendRequestToCreateDirectory(status().isForbidden(), ftpServerPOJO.getId(), "/data/yo");
		sendRequestToCreateDirectory(status().isForbidden(), ftpServerPOJO.getId(), "/home/myDir/bloup");
	}

	@Test
	public void canDeleteDirectoryIfEmpty() throws Exception {
		Assertions.assertTrue(fakeFtpServer.getFileSystem().isDirectory("/dev"));

		sendRequestToDeleteDirectory(status().isNoContent(), ftpServerPOJO.getId(), "/dev");

		Assertions.assertFalse(fakeFtpServer.getFileSystem().isDirectory("/dev"));
	}

	@Test
	public void cannotDeleteDirectoryIfNotEmpty() throws Exception {
		Assertions.assertTrue(fakeFtpServer.getFileSystem().isDirectory("/home"));
		Assertions.assertTrue(fakeFtpServer.getFileSystem().isFile("/home/text.txt"));

		sendRequestToDeleteDirectory(status().isForbidden(), ftpServerPOJO.getId(), "/home");

	}

	@Test
	public void canNotDeleteDirectoryIfPathWrong() throws Exception {
		Assertions.assertFalse(fakeFtpServer.getFileSystem().isDirectory("/home/blob"));

		sendRequestToDeleteDirectory(status().isForbidden(), ftpServerPOJO.getId(), "/home/blob");
	}

	@Test
	public void canRenameDirectory() throws Exception {
		Assertions.assertTrue(fakeFtpServer.getFileSystem().isDirectory("/home"));
		Assertions.assertTrue(fakeFtpServer.getFileSystem().isFile("/home/text.txt"));

		sendRequestToRenameDirectory(status().isOk(), ftpServerPOJO.getId(), "/home", "/newHome");

		Assertions.assertFalse(fakeFtpServer.getFileSystem().isDirectory("/home"));
		Assertions.assertTrue(fakeFtpServer.getFileSystem().isDirectory("/newHome"));
		Assertions.assertTrue(fakeFtpServer.getFileSystem().isFile("/newHome/text.txt"));
	}

	@Test
	public void cannotRenameDirectoryToAlreadyExistingOne() throws Exception {
		Assertions.assertTrue(fakeFtpServer.getFileSystem().isDirectory("/home"));
		Assertions.assertTrue(fakeFtpServer.getFileSystem().isDirectory("/dev"));

		sendRequestToRenameDirectory(status().isForbidden(), ftpServerPOJO.getId(), "/home", "/dev");
	}

	@Test
	public void cannotRenameDirectoryIfPathIsWrong() throws Exception {
		sendRequestToRenameDirectory(status().isForbidden(), ftpServerPOJO.getId(), "/homeBad", "/newHome");
	}

	@Test
	public void canRenameFile() throws Exception {
		Assertions.assertTrue(fakeFtpServer.getFileSystem().isFile("/home/text.txt"));
		Assertions.assertFalse(fakeFtpServer.getFileSystem().isFile("/dev/text.json"));

		sendRequestToRenameFile(status().isOk(), ftpServerPOJO.getId(), "/home/text.txt", "/dev/text.json");

		Assertions.assertFalse(fakeFtpServer.getFileSystem().isFile("/home/text.txt"));
		Assertions.assertTrue(fakeFtpServer.getFileSystem().isFile("/dev/text.json"));
	}

	@Test
	public void cannotRenameFileIfFileDoesNotExist() throws Exception {
		sendRequestToRenameFile(status().isForbidden(), ftpServerPOJO.getId(), "/blip/text.txt", "/dev/text.json");
	}

	@Test
	public void cannotRenameFileIfNameConflicts() throws Exception {
		sendRequestToRenameFile(status().isForbidden(), ftpServerPOJO.getId(), "/home/text.txt", "/home/run.exe");
	}

	@Test
	public void cannotRenameFileIfRenamedFileIsInANonExistentDirectory() throws Exception {
		sendRequestToRenameFile(status().isForbidden(), ftpServerPOJO.getId(), "/home/text.txt", "/blip/text.json");
	}

	@Test
	public void canDeleteFile() throws Exception {
		Assertions.assertTrue(fakeFtpServer.getFileSystem().isFile("/home/text.txt"));

		sendRequestToDeleteFile(status().isNoContent(), ftpServerPOJO.getId(), "/home/text.txt");

		Assertions.assertFalse(fakeFtpServer.getFileSystem().isFile("/home/text.txt"));
	}

	@Test
	public void cannotDeleteFileIfPathIsWrong() throws Exception {
		sendRequestToDeleteFile(status().isForbidden(), ftpServerPOJO.getId(), "/blip/text.txt");
	}

	@Test
	public void cannotDeleteFileIfFileIsDirectory() throws Exception {
		sendRequestToDeleteFile(status().isForbidden(), ftpServerPOJO.getId(), "/home");
	}


}
