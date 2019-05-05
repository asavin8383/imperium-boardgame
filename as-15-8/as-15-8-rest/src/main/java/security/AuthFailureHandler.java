package security;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import exception.ExceptionResponseBody;



public class AuthFailureHandler implements AuthenticationFailureHandler {

	@Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException e) throws IOException, ServletException {
 
        HttpStatus status = HttpStatus.UNAUTHORIZED;
 
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
 
        ExceptionResponseBody body = new ExceptionResponseBody(
                status, e.getLocalizedMessage(), e.toString());
 
        PrintWriter writer = response.getWriter();
        writer.print(new ObjectMapper().writeValueAsString(body));
        writer.flush();
    }
}
