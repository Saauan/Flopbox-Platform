package saauan.flopbox;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@Profile("!test")
public class DatabaseConfig {

	@Value("${spring.datasource.uri}")
	private String dbUriString;

	@Bean
	public DataSource dataSource() {
		URI dbUri;
		try {
			dbUri = new URI(dbUriString);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		String username = dbUri.getUserInfo().split(":")[0];
		String password = dbUri.getUserInfo().split(":")[1];
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri
				.getPath() + "?sslmode=require";

		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(dbUrl);
		config.setUsername(username);
		config.setPassword(password);
		return new HikariDataSource(config);
	}
}
