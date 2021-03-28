package saauan.flopbox;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
		info = @Info(
				title = "FlopBox API",
				version = "v1.0.0",
				description = "This app provides REST API for interacting with a ftp server")
)
@SecurityScheme(
		name = "Bearer token",
		type = SecuritySchemeType.OAUTH2,
		in = SecuritySchemeIn.HEADER,
		bearerFormat = "token"
)
@SpringBootApplication
public class FlopBoxApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlopBoxApplication.class, args);
	}

}
