package saauan.flopbox.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import saauan.flopbox.user.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServerRepository extends JpaRepository<Server, Integer> {
	@Deprecated
	Server findByUrl(String url);

	Server findByUserAndUrl(User user, String url);

	Optional<Server> findByIdAndUser(Integer id, User user);

	List<Server> findByUser(User user);
}
