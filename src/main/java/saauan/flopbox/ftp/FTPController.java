package saauan.flopbox.ftp;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import saauan.flopbox.Utils;

import java.util.Map;

/**
 * Controller for FTP commands endpoints
 */
@RestController()
@RequestMapping("/servers/{serverId}/")
public class FTPController {

	public static final String FTP_USERNAME = "FTP-Username";
	public static final String FTP_PASSWORD = "FTP-Password";
	public static final String DIRECTORIES = "/directories";
	public static final String FILES = "/files";
	public static final String LIST = "/list";
	private final FTPService ftpService;

	public FTPController(FTPService ftpService) {
		this.ftpService = ftpService;
	}

	@GetMapping(LIST)
	public Map<String, Object> list(@PathVariable int serverId, @RequestParam String path,
									@RequestHeader HttpHeaders headers) {
		return Utils.mapify(ftpService.list(serverId,
				Utils.getToken(headers),
				path,
				Utils.getHeaderValue(headers, FTP_USERNAME),
				Utils.getHeaderValue(headers, FTP_PASSWORD)),
				"files");
	}

	@GetMapping(FILES)
	public ResponseEntity<Resource> downloadFile(@PathVariable int serverId, @RequestParam String path,
												 @RequestHeader HttpHeaders headers, @RequestParam boolean binary) {
		Resource resource = ftpService.downloadFile(serverId,
				Utils.getToken(headers),
				path,
				Utils.getHeaderValue(headers, FTP_USERNAME),
				Utils.getHeaderValue(headers, FTP_PASSWORD),
				binary);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}

	protected void uploadFile(MultipartFile file,
							  int serverId,
							  String path,
							  HttpHeaders headers,
							  boolean binary) {
		ftpService.store(serverId,
				Utils.getToken(headers),
				path,
				Utils.getHeaderValue(headers, FTP_USERNAME),
				Utils.getHeaderValue(headers, FTP_PASSWORD),
				file, binary);
	}

	@PostMapping(FILES)
	public void uploadMultipleFiles(@RequestParam("file") MultipartFile[] files,
									@PathVariable int serverId,
									@RequestParam String[] path,
									@RequestHeader HttpHeaders headers,
									@RequestParam boolean[] binary) {
		if (path.length != files.length || path.length != binary.length) {
			throw new IllegalArgumentException("There must be as many paths and binary arguments as files");
		}
		for (int i = 0; i < files.length; i++) {
			uploadFile(files[i], serverId, path[i], headers, binary[i]);
		}
	}

	@PatchMapping(FILES)
	public void renameFile(@PathVariable int serverId,
						   @RequestParam String path,
						   @RequestParam String to,
						   @RequestHeader HttpHeaders headers) {
		ftpService.renameFile(serverId,
				Utils.getToken(headers),
				path,
				to,
				Utils.getHeaderValue(headers, FTP_USERNAME),
				Utils.getHeaderValue(headers, FTP_PASSWORD));
	}

	@DeleteMapping(FILES)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteFile(@PathVariable int serverId,
						   @RequestParam String path,
						   @RequestHeader HttpHeaders headers) {
		ftpService.deleteFile(serverId,
				Utils.getToken(headers),
				path,
				Utils.getHeaderValue(headers, FTP_USERNAME),
				Utils.getHeaderValue(headers, FTP_PASSWORD));
	}

	@GetMapping(DIRECTORIES)
	public ResponseEntity<Resource> downloadZipirectory(@PathVariable int serverId,
														@RequestParam String path,
														@RequestHeader HttpHeaders headers) {
		Resource resource = ftpService.downloadDirectory(serverId,
				Utils.getToken(headers),
				path,
				Utils.getHeaderValue(headers, FTP_USERNAME),
				Utils.getHeaderValue(headers, FTP_PASSWORD));
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}

	@PostMapping(DIRECTORIES)
	public void createDirectory(@PathVariable int serverId,
								@RequestParam String path,
								@RequestHeader HttpHeaders headers) {
		ftpService.createDirectory(serverId,
				Utils.getToken(headers),
				path,
				Utils.getHeaderValue(headers, FTP_USERNAME),
				Utils.getHeaderValue(headers, FTP_PASSWORD));
	}

	@DeleteMapping(DIRECTORIES)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteDirectory(@PathVariable int serverId,
								@RequestParam String path,
								@RequestHeader HttpHeaders headers) {
		ftpService.deleteDirectory(serverId,
				Utils.getToken(headers),
				path,
				Utils.getHeaderValue(headers, FTP_USERNAME),
				Utils.getHeaderValue(headers, FTP_PASSWORD));
	}

	@PatchMapping(DIRECTORIES)
	public void renameDirectory(@PathVariable int serverId,
								@RequestParam String path,
								@RequestParam String to,
								@RequestHeader HttpHeaders headers) {
		ftpService.renameDirectory(serverId,
				Utils.getToken(headers),
				path,
				to,
				Utils.getHeaderValue(headers, FTP_USERNAME),
				Utils.getHeaderValue(headers, FTP_PASSWORD));
	}
}
