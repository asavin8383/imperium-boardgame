package common;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
@EnableGlobalAuthentication
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

   @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	   auth
	   	.ldapAuthentication()
	   	.userDnPatterns("cn={0}")
		//.groupSearchBase("ou=roles")
		.contextSource()
		.url("ldap://192.168.1.52:389/OU=users,OU=AS_15_8,DC=designdiv,DC=ecleasing,DC=ru");
    }
   
   @Override
    protected void configure(HttpSecurity http) throws Exception {
	   http
	   	.authorizeRequests()
	   	.antMatchers("/css/**").permitAll()
	   	.anyRequest()
		.fullyAuthenticated().and().formLogin();
    }
	   
}
