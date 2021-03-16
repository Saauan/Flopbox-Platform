package saauan.flopbox;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpHeaders;
import saauan.flopbox.exceptions.ResourceNotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
		if (object.isEmpty()) {
			logAndThrow(log, ResourceNotFoundException.class, String.format("resource %s was not found", id));
			assert false : "Should never arrive here";
		}
		return object.get();
	}

	public String getToken(HttpHeaders headers) {
		List<String> auth = headers.get(HttpHeaders.AUTHORIZATION);
		assert auth != null;
		return StringUtils.removeStart(auth.stream().findFirst().orElseThrow(), "Bearer").trim();
	}

	public String getHeaderValue(HttpHeaders headers, String key) {
		List<String> fields = headers.get(key);
		if (fields == null || fields.size() == 0) {
			return "";
		}
		return fields.get(0);
	}

	public Map<String, Object> mapify(Object objectToMap, String objectName) {
		Map<String, Object> map = new HashMap<>();
		map.put(objectName, objectToMap);
		return map;
	}
}
