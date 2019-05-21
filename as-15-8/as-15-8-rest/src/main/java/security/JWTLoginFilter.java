package security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import model.user.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class JWTLoginFilter extends AbstractAuthenticationProcessingFilter {

	private final String jwtSecret;
    private final long ttl_msec;
 
    public JWTLoginFilter(String url, AuthenticationManager authManager, String jwtSecret, long ttl_msec) {
        super(new AntPathRequestMatcher(url));
        this.jwtSecret = jwtSecret;
        this.ttl_msec = ttl_msec;
        setAuthenticationManager(authManager);
        setAuthenticationFailureHandler(new AuthFailureHandler());
    }
 
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException, IOException {
    	AccountCredentials creds = new ObjectMapper().readValue(req.getInputStream(), AccountCredentials.class);
        AuthenticationManager authenticationManager = getAuthenticationManager();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(creds.getUsername(), creds.getPassword());
        return authenticationManager.authenticate(authentication);
    }
 
    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
                                            Authentication auth) throws IOException {
        User user = new User(((LdapUserDetails)auth.getPrincipal()).getUsername());
        new TokenAuthenticationHelper(jwtSecret).addAuthentication(
                res, user, auth.getAuthorities(), ttl_msec);
        log.info("Login Successful (username={})", user.getUserName());
    }
 
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        super.unsuccessfulAuthentication(request, response, failed);
        log.info("Login Failed ({})", failed.getMessage());
    }
 
    static class AccountCredentials {
 
        private String username;
        private String password;
 
        String getUsername() {
            return username;
        }
 
        void setUsername(String username) {
            this.username = username;
        }
 
        String getPassword() {
            return password;
        }
 
        void setPassword(String password) {
            this.password = password;
        }
    }
}
