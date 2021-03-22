package saauan.flopbox.ftp;

/**
 * Thrown when there is an error while performing an operation on an FTP Server
 */
public class FTPOperationException extends RuntimeException {
	public FTPOperationException(String message) {
		super(message);
	}
}
