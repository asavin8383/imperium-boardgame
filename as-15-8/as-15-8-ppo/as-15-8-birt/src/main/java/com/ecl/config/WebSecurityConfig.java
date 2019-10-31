package com.ecl.config;

import com.ecl.security.JWTAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@SuppressWarnings("unused")
@Slf4j
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.app.jwt.secret}")
    public String jwtSecret;

    @Value("${spring.app.cors.origin}")
    public String corsOrigin;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin(corsOrigin);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    @Override
	protected void configure(HttpSecurity http) throws Exception {
        http
            .cors()
            .and()
                .csrf().disable()
            .authorizeRequests()
//                .anyRequest().permitAll();
                .anyRequest().hasRole("GIS_USER")
            .and()
                .addFilterBefore(new JWTAuthenticationFilter(jwtSecret), UsernamePasswordAuthenticationFilter.class);
	}

}
