package com.ecl.security;

import com.ecl.user.UserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

class TokenAuthenticationHelper {

    private static final String COOKIE_BEARER = "COOKIE-BEARER";
    private static final String AUTHORITIES_KEY = "roles";
    private static final String GIVEN_NAME = "given_name";
    private static final String FAMILY_NAME = "family_name";

    private final String jwtSecret;

    TokenAuthenticationHelper(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    Authentication getAuthentication(HttpServletRequest request) {
        String token = null;
        Cookie cookie = WebUtils.getCookie(request, COOKIE_BEARER);
        if (cookie != null) {
            token = cookie.getValue();
        }

        if (token != null) {
            Claims body = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();
            final UserInfo info = convertClaimsToInfo(body);
            final String auths = body.get(AUTHORITIES_KEY).toString();

            final Collection<? extends GrantedAuthority> authorities = (auths == null || auths.isEmpty()) ?
                    null : Arrays.stream(auths.split(",")).map(SimpleGrantedAuthority::new).collect(Collectors.toList());

            return info.getUsername() != null ? new UsernamePasswordAuthenticationToken(info, null, authorities) : null;
        }
        return null;
    }

    private static UserInfo convertClaimsToInfo(Claims claims) {
        UserInfo info = new UserInfo(claims.getSubject());
        info.setFirstName(claims.get(GIVEN_NAME, String.class));
        info.setSecondName(claims.get(FAMILY_NAME, String.class));
        return info;
    }
}