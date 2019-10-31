package com.ecl.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.java.Log;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Log
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private String jwtSecret;

    public JWTAuthenticationFilter(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }


    @Override
    protected void doFilterInternal(final HttpServletRequest req, final HttpServletResponse resp, final FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Authentication authentication = new TokenAuthenticationHelper(jwtSecret).getAuthentication(req);
            log.info("Authentication" + authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(req, resp);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
        }
    }
}