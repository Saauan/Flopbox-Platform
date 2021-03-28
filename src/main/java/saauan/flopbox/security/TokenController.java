package saauan.flopbox.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

	@Operation(summary = "Authenticate a user",
			responses = {
					@ApiResponse(responseCode = "200", description = "Authenticated", content = {
							@Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
									schema = @Schema(implementation = String.class))})
			}
	)
	@PostMapping("/token")
	public String getToken(@RequestParam("username") final String username,
						   @RequestParam("password") final String password) {
		String token = userService.login(username, password);
		if (StringUtils.isEmpty(token)) {
			return "no token found";
		}
		return token;
	}

	@Operation(summary = "Log out a user",
			responses = {
					@ApiResponse(responseCode = "200", description = "Log out successful")
			}
	)
	@PostMapping("/logout")
	public void logout(@RequestHeader HttpHeaders headers) {
		userService.logout(Utils.getToken(headers));
	}
}
