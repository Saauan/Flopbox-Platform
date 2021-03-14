package saauan.flopbox.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

	Optional<User> findByUsernameAndPassword(String username, String password);

	Optional<User> findByToken(String token);

	default Optional<User> login(String username, String password) {
		return findByUsernameAndPassword(username, password);
	}
}
