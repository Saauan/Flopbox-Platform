package saauan.flopbox.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.net.URL;

@Repository
public interface ServerRepository extends JpaRepository<Server, Integer> {
	public Server findByUrl(URL url);
}
