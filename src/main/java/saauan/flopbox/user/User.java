package saauan.flopbox.user;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table
@EqualsAndHashCode
@ToString
public class User {
	@NonNull
	@Id
	private String username;
	@NonNull
	private String password;
}
