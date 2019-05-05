package security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import user.UserInfo;

public class TokenAuthenticationHelper {
	
    private static final String AUTHORITIES_KEY = "roles";
    private static final String GIVEN_NAME = "given_name";
    private static final String FAMILY_NAME = "family_name";
    
    static final String HEADER_STRING = "X-AUTH-TOKEN";
 
    private final String jwtSecret;
 
    TokenAuthenticationHelper(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }
 
    void addAuthentication(HttpServletResponse res, UserInfo userInfo, Collection<? extends GrantedAuthority> authorities, long ttl_msec) throws UnsupportedEncodingException, IOException {
        final String auths = authorities.stream().map(GrantedAuthority::getAuthority).filter(s->s.startsWith("ROLE_")).collect(Collectors.joining(","));
        String JWT = Jwts.builder()
                .setSubject(userInfo.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + ttl_msec))
                .signWith(SignatureAlgorithm.HS512, jwtSecret.getBytes(StandardCharsets.UTF_8))
                .claim(AUTHORITIES_KEY, auths)
                .addClaims(convertInfoToMap(userInfo))
                .compact();
		if (JWT != null) {
			res.setContentType("application/json;charset=UTF-8");
			res.setHeader("Cache-Control", "no-cache");
			try {
				res.getWriter().write(createJson(JWT).toString());
			} catch (IOException e) {
				res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error! Can't write token to the response body");
			}
		}
    }
 
    Authentication getAuthentication(HttpServletRequest request) throws UnsupportedEncodingException {
    	String token = request.getHeader(HEADER_STRING);
        if (token != null) {
            // parse the token.
        	Claims body = Jwts.parser().setSigningKey(jwtSecret.getBytes(StandardCharsets.UTF_8)).parseClaimsJws(token).getBody();
 
        	final UserInfo info = convertClaimsToInfo(body);
            final String auths = body.get(AUTHORITIES_KEY).toString();
 
            final Collection<? extends GrantedAuthority> authorities = (auths == null || auths.isEmpty()) ?
                    null : Arrays.stream(auths.split(",")).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
 
            return info.getUsername() != null ? new UsernamePasswordAuthenticationToken(info, null, authorities) : null;
        }
        return null;
        
    }
 
    private static Map<String, Object> convertInfoToMap(UserInfo info) {
        Map<String, Object> result = new HashMap<>();
        result.put(GIVEN_NAME, info.getFirstName());
        result.put(FAMILY_NAME, info.getSecondName());
        return result;
    }
 
    private static UserInfo convertClaimsToInfo(Claims claims) {
        UserInfo info = new UserInfo(claims.getSubject());
        info.setFirstName(claims.get(GIVEN_NAME, String.class));
        info.setSecondName(claims.get(FAMILY_NAME, String.class));
        return info;
    }
    
    private static ObjectNode createJson(String token) {
    	ObjectMapper mapper = new ObjectMapper();
    	ObjectNode node = mapper.createObjectNode();
    	node.put("access_token", token);
    	return node;
    }
}
