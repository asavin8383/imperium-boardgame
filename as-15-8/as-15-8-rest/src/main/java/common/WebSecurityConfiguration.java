package common;

import advices.AuthenticationEntryPointImpl;
import exceptions.AS_15_8_Exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import security.JWTAuthenticationFilter;
import security.JWTLoginFilter;
import services.userDetails.CustomUserDetailsMapper;

import javax.sql.DataSource;
import java.util.Collections;


@Configuration
@Slf4j
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Value("${ldap.urls}")
	private String ldapUrls;
	
	@Value("${ldap.domain}")
	private String adDomain;
	
	@Value("${ldap.base.dn}")
	private String ldapBaseDn;
	
	@Value("${ldap.user.dn.pattern}")
	private String ldapUserDnPattern;
	
	/*@Value("${ldap.login.filter}")
	private String ldapFilter;*/
	
	@Value("${spring.app.cors.origin}")
    public String corsOrigin;
	
	@Value("${spring.app.jwt.secret}")
	public String jwtSecret;
	
	/*@Value("${spring.app.jwt.ttl}")
    public long jwtTTLSec;*/

	@Autowired
	private CustomUserDetailsMapper userDetailsMapper;

    @Autowired
    JdbcTemplate jdbcTemplate;

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

    @Bean
    public JWTLoginFilter jwtLoginFilter(){
        return new JWTLoginFilter("/gettoken", authenticationManager(), jwtSecret, getJwtTTLSec() * 1000);
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
                .antMatchers(HttpMethod.GET, "/results/screenshot").permitAll()
                .antMatchers(HttpMethod.GET, "/results/etalon_screenshot").permitAll()
                .anyRequest().authenticated()
            .and()
                .logout()
                .logoutUrl("/api/logout")
                .logoutSuccessHandler(
                        (httpServletRequest, httpServletResponse, authentication) -> log.info("Logout Successful"))
            .and()
                .addFilterBefore(jwtLoginFilter(),
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
        //provider.setSearchFilter(ldapFilter);
        provider.setUserDetailsContextMapper(userDetailsMapper);
        return provider;
    }

    private long getJwtTTLSec(){
        String sql = "select value from system.system_parameters where key='jwt_ttl_sec'";
        String value = jdbcTemplate.queryForObject(sql, String.class);
        if (value==null){
            log.error("Error getting ttl for token from DB. Query result is null: " + sql);
            throw new AS_15_8_Exception("Error getting ttl for token from DB. Query result is null: " + sql);
        }
        return Long.valueOf(value);
    }



}
