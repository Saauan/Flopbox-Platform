package saauan.flopbox.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import saauan.flopbox.user.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServerRepository extends JpaRepository<Server, Integer> {
	@Deprecated
	public Server findByUrl(String url);

	public Server findByUserAndUrl(User user, String url);

	public Optional<Server> findByIdAndUser(Integer id, User user);

	public List<Server> findByUser(User user);
}
