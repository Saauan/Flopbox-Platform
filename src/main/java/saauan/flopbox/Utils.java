package saauan.flopbox;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.logging.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import saauan.flopbox.exceptions.ResourceNotFoundException;

import java.util.Optional;

@UtilityClass
public class Utils {

	@SneakyThrows
	public void logAndThrow(Log log, Class<? extends RuntimeException> exceptionClass, String message) {
		log.error(message);
		throw exceptionClass.getDeclaredConstructor(String.class).newInstance(message);
	}

	public <T, ID> T findObjectOrThrow(JpaRepository<T, ID> repository, ID id, Log log) {
		Optional<T> object = repository.findById(id);
		if(object.isEmpty()) {
			logAndThrow(log, ResourceNotFoundException.class, String.format("resource %s was not found", id));
			assert false: "Should never arrive here";
		}
		return object.get();
	}
}
