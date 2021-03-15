package saauan.flopbox.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import saauan.flopbox.user.User;

import java.net.URL;
import java.util.List;

@Repository
public interface ServerRepository extends JpaRepository<Server, Integer> {
	@Deprecated
	public Server findByUrl(URL url);
	public Server findByUserAndUrl(User user, URL url);
	public List<Server> findByUser(User user);
}
