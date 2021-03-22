package saauan.flopbox.ftp;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import saauan.flopbox.Utils;

import java.util.Arrays;
import java.util.Map;

@RestController()
@RequestMapping("/servers/{serverId}/files")
public class FTPController {

	public static final String FTP_USERNAME = "FTP-Username";
	public static final String FTP_PASSWORD = "FTP-Password";
	private final FTPService ftpService;

	public FTPController(FTPService ftpService) {
		this.ftpService = ftpService;
	}

	@GetMapping("/list")
	public Map<String, Object> list(@PathVariable int serverId, @RequestParam String path,
									@RequestHeader HttpHeaders headers) {
		return Utils.mapify(ftpService.list(serverId,
				Utils.getToken(headers),
				path,
				Utils.getHeaderValue(headers, FTP_USERNAME),
				Utils.getHeaderValue(headers, FTP_PASSWORD)),
				"files");
	}

	@GetMapping()
	public ResponseEntity<Resource> downloadFile(@PathVariable int serverId, @RequestParam String path,
												 @RequestHeader HttpHeaders headers) {
		Resource resource = ftpService.loadAsResource(serverId,
				Utils.getToken(headers),
				path,
				Utils.getHeaderValue(headers, FTP_USERNAME),
				Utils.getHeaderValue(headers, FTP_PASSWORD));
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}

	@PostMapping()
	public void uploadFile(@RequestParam("file") MultipartFile file, @PathVariable int serverId,
						   @RequestParam String path,
						   @RequestHeader HttpHeaders headers) {
		ftpService.store(serverId,
				Utils.getToken(headers),
				path,
				Utils.getHeaderValue(headers, FTP_USERNAME),
				Utils.getHeaderValue(headers, FTP_PASSWORD),
				file);
	}

	public void uploadMultipleFiles(@RequestParam("files") MultipartFile[] files, @PathVariable int serverId,
									@RequestParam String path,
									@RequestHeader HttpHeaders headers) {
		Arrays.stream(files).forEach(file -> uploadFile(file, serverId, path, headers));
	}
}
