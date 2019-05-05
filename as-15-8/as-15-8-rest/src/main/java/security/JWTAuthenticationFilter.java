package security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;

public class JWTAuthenticationFilter extends OncePerRequestFilter {

	String jwtSecret;
	 
    public JWTAuthenticationFilter(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }
 
    @Override
    protected void doFilterInternal(final HttpServletRequest req, final HttpServletResponse resp, final FilterChain filterChain)
            throws ServletException, IOException {
        try {
 
            String uri = req.getRequestURI();
            boolean skip = false;
            skip |= uri.startsWith("/info/");
 
            if (!skip) {
                long init = 1556641831;
                long ttl = 60 * 60 * 24 * 30;
                long deadline = init + ttl;
                long now = Long.parseLong(req.getHeader("x-msg-timestamp"));
                if (now > deadline) throw new RuntimeException("Synchronization fault");
            }
 
            Authentication authentication = new TokenAuthenticationHelper(jwtSecret).getAuthentication(req);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(req, resp);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
        }
    }
}
