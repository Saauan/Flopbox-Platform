package saauan.flopbox.ftp;

/**
 * Thrown when there was an error while trying to login to the FTP server
 */
public class FTPLoginException extends FTPOperationException {
	public FTPLoginException(String message) {
		super(message);
	}
}
