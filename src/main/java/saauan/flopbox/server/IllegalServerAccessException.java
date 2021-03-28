package saauan.flopbox.server;

/**
 * Thrown when an user tries to access a server that does not belong to him.
 */
public class IllegalServerAccessException extends RuntimeException{
	public IllegalServerAccessException(String message) {
		super(message);
	}
}
