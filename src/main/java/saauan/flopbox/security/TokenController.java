package saauan.flopbox.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import saauan.flopbox.Utils;
import saauan.flopbox.user.UserService;

@RestController
public class TokenController {

	@Autowired
	private UserService userService;

	@PostMapping("/token")
	public String getToken(@RequestParam("username") final String username,
						   @RequestParam("password") final String password) {
		String token = userService.login(username, password);
		if (StringUtils.isEmpty(token)) {
			return "no token found";
		}
		return token;
	}

	@PostMapping("/logout")
	public void logout(@RequestHeader HttpHeaders headers) {
		userService.logout(Utils.getToken(headers));
	}
}
