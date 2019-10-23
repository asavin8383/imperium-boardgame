package security;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import exceptions.AS_15_8_API_Error;



public class AuthFailureHandler implements AuthenticationFailureHandler {

	@Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException e) throws IOException {
 
        HttpStatus status = HttpStatus.UNAUTHORIZED;
 
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
 
        AS_15_8_API_Error body = new AS_15_8_API_Error(
                status, e.getLocalizedMessage(), e);
 
        PrintWriter writer = response.getWriter();
        writer.print(new ObjectMapper().writeValueAsString(body));
        writer.flush();
    }
}
