package saauan.flopbox;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.logging.Log;

@UtilityClass
public class Utils {

	@SneakyThrows
	public void logAndThrow(Log log, Class<? extends RuntimeException> exceptionClass, String message) {
		log.error(message);
		throw exceptionClass.getDeclaredConstructor(String.class).newInstance(message);
	}
}
