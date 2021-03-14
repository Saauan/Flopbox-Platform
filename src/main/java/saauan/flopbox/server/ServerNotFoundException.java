package saauan.flopbox.server;

public class ServerNotFoundException extends RuntimeException{
	public ServerNotFoundException(String message) {
		super(message);
	}
}
