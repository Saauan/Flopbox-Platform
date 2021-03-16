package saauan.flopbox.ftp;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
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
}
