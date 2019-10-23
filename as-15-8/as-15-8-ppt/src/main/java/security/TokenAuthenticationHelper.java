package security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import model.user.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

class TokenAuthenticationHelper {
	
    private static final String AUTHORITIES_KEY = "roles";
    private static final String GIVEN_NAME = "given_name";
    private static final String FAMILY_NAME = "family_name";
    
    private static final String HEADER_STRING = "X-AUTH-TOKEN";
 
    private final String jwtSecret;
 
    TokenAuthenticationHelper(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }
 
    void addAuthentication(HttpServletResponse res, User user, Collection<? extends GrantedAuthority> authorities, long ttl_msec) throws IOException {
        final String auths = authorities.stream().map(GrantedAuthority::getAuthority).filter(s->s.startsWith("ROLE_")).collect(Collectors.joining(","));
        String JWT = Jwts.builder()
                .setSubject(user.getUserName())
                .setExpiration(new Date(System.currentTimeMillis() + ttl_msec))
                .signWith(SignatureAlgorithm.HS512, jwtSecret.getBytes(StandardCharsets.UTF_8))
                .claim(AUTHORITIES_KEY, auths)
                .addClaims(convertInfoToMap(user))
                .compact();
		if (JWT != null) {
			res.setContentType("application/json;charset=UTF-8");
			res.setHeader("Cache-Control", "no-cache");
			try {
				res.getWriter().write(createJson(JWT, auths).toString());
			} catch (Exception e) {
				res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error! Can't write token to the response body");
			}
		}
    }
 
    Authentication getAuthentication(HttpServletRequest request) {
    	String token = request.getHeader(HEADER_STRING);
        if (token != null) {
            // parse the token.
        	Claims body = Jwts.parser().setSigningKey(jwtSecret.getBytes(StandardCharsets.UTF_8)).parseClaimsJws(token).getBody();
 
        	final User user = convertClaimsToInfo(body);
            final String auths = body.get(AUTHORITIES_KEY).toString();
 
            final Collection<? extends GrantedAuthority> authorities = (auths == null || auths.isEmpty()) ?
                    null : Arrays.stream(auths.split(",")).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
 
            return user.getUserName() != null ? new UsernamePasswordAuthenticationToken(user, null, authorities) : null;
        }
        return null;
        
    }
 
    private static Map<String, Object> convertInfoToMap(User user) {
        Map<String, Object> result = new HashMap<>();
        result.put(GIVEN_NAME, user.getFirstName());
        result.put(FAMILY_NAME, user.getSecondName());
        return result;
    }
 
    private static User convertClaimsToInfo(Claims claims) {
        User user = new User();
        user.setUserName(claims.getSubject());
        user.setFirstName(claims.get(GIVEN_NAME, String.class));
        user.setSecondName(claims.get(FAMILY_NAME, String.class));
        return user;
    }
    
    private static ObjectNode createJson(String token, String role) {
    	ObjectMapper mapper = new ObjectMapper();
    	ObjectNode node = mapper.createObjectNode();
    	node.put("access_token", token);
    	node.put("role", role);
    	return node;
    }
}
