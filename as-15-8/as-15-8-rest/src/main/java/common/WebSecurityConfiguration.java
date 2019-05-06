package common;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import advices.AuthenticationEntryPointImpl;
import lombok.extern.slf4j.Slf4j;
import security.JWTAuthenticationFilter;
import security.JWTLoginFilter;
import user.CustomUserDetailsMapper;


@Configuration
@Slf4j
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Value("${ldap.urls}")
	private String ldapUrls;
	
	@Value("${ldap.domain}")
	private String adDomain;
	
	@Value("${ldap.base.dn}")
	private String ldapBaseDn;
	
	@Value("${ldap.user.dn.pattern}")
	private String ldapUserDnPattern;
	
	@Value("${ldap.login.filter}")
	private String ldapFilter;
	
	@Value("${spring.app.cors.origin}")
    public String corsOrigin;
	
	@Value("${spring.app.jwt.secret}")
	public String jwtSecret;
	
	@Value("${spring.app.jwt.ttl}")
    public long jwtTTLSec;
 
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
	         .csrf()
	         	.disable()
	         .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/gettoken").permitAll()
                .anyRequest().authenticated()
            .and()
                .logout()
                .logoutUrl("/api/logout")
                .logoutSuccessHandler(
                        (httpServletRequest, httpServletResponse, authentication) -> log.info("Logout Successful"))
            .and()
                .addFilterBefore(new JWTLoginFilter("/gettoken", authenticationManager(), jwtSecret, jwtTTLSec * 1000),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JWTAuthenticationFilter(jwtSecret), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPointImpl());
	}
	
	
	@Override
    protected void configure(AuthenticationManagerBuilder authManagerBuilder) throws Exception {
        authManagerBuilder.authenticationProvider(activeDirectoryLdapAuthenticationProvider()).userDetailsService(userDetailsService());
    }
 
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(Collections.singletonList(activeDirectoryLdapAuthenticationProvider()));
    }
 
    @Bean
    public AuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
        ActiveDirectoryLdapAuthenticationProvider provider =
                new ActiveDirectoryLdapAuthenticationProvider(adDomain, ldapUrls, ldapBaseDn);
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setUseAuthenticationRequestCredentials(true);
        provider.setSearchFilter(ldapFilter);
        provider.setUserDetailsContextMapper(new CustomUserDetailsMapper());
        return provider;
    }
	   
}
