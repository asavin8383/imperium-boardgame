package common;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
    @Override
    public void configure(final HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/actuator/bus-refresh/**").hasRole("MANAGE_CONFIGURATIONS")
                .antMatchers("/robots/**").hasRole("MANAGE_CONFIGURATIONS")
                .antMatchers(HttpMethod.GET, "/**").hasRole("CONFIG_CLIENT")
                .antMatchers(HttpMethod.POST, "/mode/**", "/mode/current/**").permitAll()
            .anyRequest().authenticated()
        .and().httpBasic().disable().csrf().disable();
    }
}
