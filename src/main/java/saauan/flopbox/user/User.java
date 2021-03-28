package saauan.flopbox.user;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * A user is an entity that can use the FlopBox application.
 * <p>
 * He owns a token that allows him to authenticate himself
 */
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user")
@EqualsAndHashCode
@ToString
public class User {
	@NonNull
	@Id
	private String username;
	@NonNull
	private String password;

	@EqualsAndHashCode.Exclude
	private String token;
}
