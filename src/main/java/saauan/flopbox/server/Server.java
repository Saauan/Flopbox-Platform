package saauan.flopbox.server;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import saauan.flopbox.user.User;

import javax.persistence.*;

/**
 * A FTP Server
 *
 * It is represented by an url, and a port to connect to.
 */
@Entity
@Table
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@JsonRootName("server")
public class Server {

	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Id
	private int id;

	@EqualsAndHashCode.Include
	@NonNull
	private String url;

	@NonNull
	private int port;

	private boolean passive;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@EqualsAndHashCode.Include
	private User user;
}
