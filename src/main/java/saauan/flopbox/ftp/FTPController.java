package saauan.flopbox.ftp;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import saauan.flopbox.Utils;

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
}
