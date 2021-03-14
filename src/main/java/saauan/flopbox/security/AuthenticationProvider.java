package saauan.flopbox.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import saauan.flopbox.user.UserService;

import java.util.Optional;

/**
 * The AuthenticationProvider is responsible to find user based on the authentication token sent by the client in the header
 */
@Component
public class AuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	@Autowired
	UserService userService;

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
												  UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken)
			throws AuthenticationException {
		//
	}

	@Override
	protected UserDetails retrieveUser(String userName, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
		Object token = usernamePasswordAuthenticationToken.getCredentials();
		return Optional.ofNullable(token)
				.map(String::valueOf)
				.flatMap(userService::findByToken)
				.orElseThrow(() -> new UsernameNotFoundException("Cannot find user with authentication token=" + token));
	}
}
