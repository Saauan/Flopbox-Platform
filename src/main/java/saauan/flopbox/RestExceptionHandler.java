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
import saauan.flopbox.server.ServerNotFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles exceptions by returning a response with an error message and an error code
 */
@RestControllerAdvice
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

	@ExceptionHandler(value = {ServerNotFoundException.class})
	protected ResponseEntity<Object> handleUnexpectedDatabaseStateException(ServerNotFoundException ex,
																			WebRequest request) {
		return handleException(HttpStatus.NOT_FOUND,
				"The request server was not found : " + ex.getMessage(), ex,
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
