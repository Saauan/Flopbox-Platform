package saauan.flopbox;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.net.ftp.FTPFile;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomFTPFile extends FTPFile {
}
