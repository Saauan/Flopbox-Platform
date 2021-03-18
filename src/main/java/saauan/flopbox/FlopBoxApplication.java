package saauan.flopbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(StorageProperties.class)
@SpringBootApplication
public class FlopBoxApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlopBoxApplication.class, args);
	}

}
