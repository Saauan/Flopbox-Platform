package saauan.flopbox.exceptions;

/**
 * Thrown when a resource should exist, but is not found
 */
public class ResourceNotFoundException extends RuntimeException{
	public ResourceNotFoundException(String message) {
		super(message);
	}
}
