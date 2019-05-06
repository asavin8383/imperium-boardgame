package advices;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		// 401
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
				"Authentication Failed : " + authException.getMessage());
	}

	/*@ExceptionHandler(value = { AccessDeniedException.class })
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		// 403
		response.sendError(HttpServletResponse.SC_FORBIDDEN,
				"Authorization Failed : " + accessDeniedException.getMessage());
	}

	@ExceptionHandler(value = { Exception.class })
	public void commence(HttpServletRequest request, HttpServletResponse response, Exception exception)
			throws IOException {
		// 500
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				"Internal Server Error : " + exception.getMessage());
	}*/
}
