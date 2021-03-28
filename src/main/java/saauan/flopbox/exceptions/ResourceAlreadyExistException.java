package saauan.flopbox.exceptions;

/**
 * Thrown when a resource already exists while trying to create it
 */
public class ResourceAlreadyExistException extends RuntimeException {
	public ResourceAlreadyExistException(String message) {
		super(message);
	}
}
