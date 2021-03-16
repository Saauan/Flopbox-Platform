package saauan.flopbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import saauan.flopbox.exceptions.ResourceNotFoundException;
import saauan.flopbox.ftp.FTPConnectException;
import saauan.flopbox.ftp.FTPLoginException;
import saauan.flopbox.ftp.FTPOperationException;
import saauan.flopbox.server.IllegalServerAccessException;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles exceptions by returning a response with an error message and an error code
 */
@RestControllerAdvice
@CommonsLog
public class RestExceptionHandler
		extends ResponseEntityExceptionHandler {


	@SneakyThrows
	private static String getBodyOfResponse(HttpStatus status, String errorMessage) {
		Map<String, Object> mapBody = new HashMap<>();
		mapBody.put("message", errorMessage);
		mapBody.put("error", status.getReasonPhrase());
		mapBody.put("status", status.value());
		return new ObjectMapper().writeValueAsString(mapBody);

	}

	@ExceptionHandler(value = {ResourceNotFoundException.class})
	protected ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex,
																	 WebRequest request) {
		log.error(ex.toString());
		return handleException(HttpStatus.NOT_FOUND,
				"The request server was not found : " + ex.getMessage(), ex,
				request);
	}

	@ExceptionHandler(value = {IllegalServerAccessException.class})
	protected ResponseEntity<Object> handleIllegalServerAccessException(IllegalServerAccessException ex,
																		WebRequest request) {
		log.error(ex.toString());
		return handleException(HttpStatus.FORBIDDEN,
				"The requested server cannot be accessed : " + ex.getMessage(), ex,
				request);
	}

	@ExceptionHandler(value = {FTPConnectException.class})
	protected ResponseEntity<Object> handleFTPConnectException(FTPConnectException ex,
															   WebRequest request) {
		log.error(ex.toString());
		return handleException(HttpStatus.INTERNAL_SERVER_ERROR,
				"Error while connecting to the FTP server : " + ex.getMessage(), ex,
				request);
	}

	@ExceptionHandler(value = {FTPLoginException.class, FTPOperationException.class})
	protected ResponseEntity<Object> handleFTPOperationException(FTPOperationException ex,
																 WebRequest request) {
		log.error(ex.toString());
		return handleException(HttpStatus.FORBIDDEN,
				"Error while performing operation on FTP Server " + ex.getMessage(), ex,
				request);
	}

	private ResponseEntity<Object> handleException(HttpStatus status, String errorMessage, Exception ex,
												   WebRequest request) {
		String bodyOfResponse = getBodyOfResponse(status, errorMessage);
		var headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return handleExceptionInternal(ex, bodyOfResponse,
				headers, status, request);
	}
}
