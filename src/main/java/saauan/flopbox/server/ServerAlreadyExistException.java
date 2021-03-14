package saauan.flopbox.server;

public class ServerAlreadyExistException extends RuntimeException {
	public ServerAlreadyExistException(String message) {
		super(message);
	}
}
