package saauan.flopbox.ftp;

import lombok.Getter;
import org.apache.commons.net.ftp.FTPClient;

/**
 * Describes if a file is text or binary
 */
@Getter
public enum FileType {
	ASCII(FTPClient.ASCII_FILE_TYPE),
	BINARY(FTPClient.BINARY_FILE_TYPE);

	private final int typeInt;

	FileType(int typeInt) {
		this.typeInt = typeInt;
	}
}
