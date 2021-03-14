package saauan.flopbox.user;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

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
//	@Type(type = "uuid-char")
	private String token;
}
