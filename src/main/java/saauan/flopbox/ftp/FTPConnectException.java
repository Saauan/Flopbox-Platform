package saauan.flopbox.ftp;

/**
 * Exception thrown when there is an IO error while connecting to a FTP server
 */
public class FTPConnectException extends RuntimeException {
	public FTPConnectException(String message, Throwable cause) {
		super(message, cause);
	}
}
