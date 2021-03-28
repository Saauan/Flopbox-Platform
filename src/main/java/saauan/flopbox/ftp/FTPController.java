package saauan.flopbox.ftp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import saauan.flopbox.FlopBoxApplication;
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

	@Operation(summary = "List files on the FTP Server",
			security = @SecurityRequirement(name = FlopBoxApplication.FLOPBOX_SECURITY),
			responses = {
					@ApiResponse(responseCode = "200", description = "List files", content = {
							@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
									array = @ArraySchema(schema = @Schema(implementation = FTPFile.class)))}),
					@ApiResponse(responseCode = "403", description = "Error during FTP operation"),
					@ApiResponse(responseCode = "404", description = "Server not found"),
					@ApiResponse(responseCode = "500", description = "Error while connecting to the FTP server")
			},
			parameters = {
					@Parameter(in = ParameterIn.HEADER, required = true, name = "FTP-Username", schema = @Schema(implementation = String.class)),
					@Parameter(in = ParameterIn.HEADER, required = true, name = "FTP-Password", schema = @Schema(implementation = String.class))
			}
	)
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

	@Operation(summary = "Download a file",
			security = @SecurityRequirement(name = FlopBoxApplication.FLOPBOX_SECURITY),
			responses = {
					@ApiResponse(responseCode = "200", description = "File download", content = {
							@Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)}),
					@ApiResponse(responseCode = "403", description = "Error during FTP operation"),
					@ApiResponse(responseCode = "404", description = "Server not found"),
					@ApiResponse(responseCode = "500", description = "Error while connecting to the FTP server")
			},
			parameters = {
					@Parameter(in = ParameterIn.HEADER, required = true, name = "FTP-Username", schema = @Schema(implementation = String.class)),
					@Parameter(in = ParameterIn.HEADER, required = true, name = "FTP-Password", schema = @Schema(implementation = String.class))
			}
	)
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

	@Operation(summary = "Upload files on the FTP Server",
			security = @SecurityRequirement(name = FlopBoxApplication.FLOPBOX_SECURITY),
			responses = {
					@ApiResponse(responseCode = "200", description = "Files uploaded"),
					@ApiResponse(responseCode = "403", description = "Error during FTP operation"),
					@ApiResponse(responseCode = "404", description = "Server not found"),
					@ApiResponse(responseCode = "500", description = "Error while connecting to the FTP server")
			},
			parameters = {
					@Parameter(in = ParameterIn.HEADER, required = true, name = "FTP-Username", schema = @Schema(implementation = String.class)),
					@Parameter(in = ParameterIn.HEADER, required = true, name = "FTP-Password", schema = @Schema(implementation = String.class))
			}
	)
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

	@Operation(summary = "Rename a file on the FTP server",
			security = @SecurityRequirement(name = FlopBoxApplication.FLOPBOX_SECURITY),
			responses = {
					@ApiResponse(responseCode = "200", description = "File renamed"),
					@ApiResponse(responseCode = "403", description = "Error during FTP operation"),
					@ApiResponse(responseCode = "404", description = "Server not found"),
					@ApiResponse(responseCode = "500", description = "Error while connecting to the FTP server")
			},
			parameters = {
					@Parameter(in = ParameterIn.HEADER, required = true, name = "FTP-Username", schema = @Schema(implementation = String.class)),
					@Parameter(in = ParameterIn.HEADER, required = true, name = "FTP-Password", schema = @Schema(implementation = String.class))
			}
	)
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

	@Operation(summary = "Delete a file on the FTP server",
			security = @SecurityRequirement(name = FlopBoxApplication.FLOPBOX_SECURITY),
			responses = {
					@ApiResponse(responseCode = "204", description = "File deleted"),
					@ApiResponse(responseCode = "403", description = "Error during FTP operation"),
					@ApiResponse(responseCode = "404", description = "Server not found"),
					@ApiResponse(responseCode = "500", description = "Error while connecting to the FTP server")
			},
			parameters = {
					@Parameter(in = ParameterIn.HEADER, required = true, name = "FTP-Username", schema = @Schema(implementation = String.class)),
					@Parameter(in = ParameterIn.HEADER, required = true, name = "FTP-Password", schema = @Schema(implementation = String.class))
			}
	)
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

	@Operation(summary = "Download a directory as a zip",
			security = @SecurityRequirement(name = FlopBoxApplication.FLOPBOX_SECURITY),
			responses = {
					@ApiResponse(responseCode = "200", description = "Directory downloaded", content = {
							@Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)}),
					@ApiResponse(responseCode = "403", description = "Error during FTP operation"),
					@ApiResponse(responseCode = "404", description = "Server not found"),
					@ApiResponse(responseCode = "500", description = "Error while connecting to the FTP server")
			},
			parameters = {
					@Parameter(in = ParameterIn.HEADER, required = true, name = "FTP-Username", schema = @Schema(implementation = String.class)),
					@Parameter(in = ParameterIn.HEADER, required = true, name = "FTP-Password", schema = @Schema(implementation = String.class))
			}
	)
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

	@Operation(summary = "Create a directory on the FTP server",
			security = @SecurityRequirement(name = FlopBoxApplication.FLOPBOX_SECURITY),
			responses = {
					@ApiResponse(responseCode = "201", description = "Directory created"),
					@ApiResponse(responseCode = "403", description = "Error during FTP operation"),
					@ApiResponse(responseCode = "404", description = "Server not found"),
					@ApiResponse(responseCode = "500", description = "Error while connecting to the FTP server")
			},
			parameters = {
					@Parameter(in = ParameterIn.HEADER, required = true, name = "FTP-Username", schema = @Schema(implementation = String.class)),
					@Parameter(in = ParameterIn.HEADER, required = true, name = "FTP-Password", schema = @Schema(implementation = String.class))
			}
	)
	@PostMapping(DIRECTORIES)
	@ResponseStatus(HttpStatus.CREATED)
	public void createDirectory(@PathVariable int serverId,
								@RequestParam String path,
								@RequestHeader HttpHeaders headers) {
		ftpService.createDirectory(serverId,
				Utils.getToken(headers),
				path,
				Utils.getHeaderValue(headers, FTP_USERNAME),
				Utils.getHeaderValue(headers, FTP_PASSWORD));
	}

	@Operation(summary = "Delete a directory on the FTP server",
			security = @SecurityRequirement(name = FlopBoxApplication.FLOPBOX_SECURITY),
			responses = {
					@ApiResponse(responseCode = "204", description = "Directory deleted"),
					@ApiResponse(responseCode = "403", description = "Error during FTP operation"),
					@ApiResponse(responseCode = "404", description = "Server not found"),
					@ApiResponse(responseCode = "500", description = "Error while connecting to the FTP server")
			},
			parameters = {
					@Parameter(in = ParameterIn.HEADER, required = true, name = "FTP-Username", schema = @Schema(implementation = String.class)),
					@Parameter(in = ParameterIn.HEADER, required = true, name = "FTP-Password", schema = @Schema(implementation = String.class))
			}
	)
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

	@Operation(summary = "Rename a directory on the FTP server",
			security = @SecurityRequirement(name = FlopBoxApplication.FLOPBOX_SECURITY),
			responses = {
					@ApiResponse(responseCode = "200", description = "Directory renamed"),
					@ApiResponse(responseCode = "403", description = "Error during FTP operation"),
					@ApiResponse(responseCode = "404", description = "Server not found"),
					@ApiResponse(responseCode = "500", description = "Error while connecting to the FTP server")
			},
			parameters = {
					@Parameter(in = ParameterIn.HEADER, required = true, name = "FTP-Username", schema = @Schema(implementation = String.class)),
					@Parameter(in = ParameterIn.HEADER, required = true, name = "FTP-Password", schema = @Schema(implementation = String.class))
			}
	)
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
