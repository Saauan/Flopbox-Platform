package saauan.flopbox.server;

import lombok.*;
import saauan.flopbox.user.User;

import javax.persistence.*;
import java.net.URL;

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
public class Server {

	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Id
	private int id;

	@NonNull
	@EqualsAndHashCode.Include
	private URL url;

	@NonNull
	private int port;

	@ManyToOne
	@EqualsAndHashCode.Include
	private User user;
}
