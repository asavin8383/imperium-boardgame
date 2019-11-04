package common;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .antMatcher("/**")
                .authorizeRequests()
                    .antMatchers(HttpMethod.GET,
                            "/",
                            "/lastn",
                            "/eureka/js/**",
                            "/eureka/css/**",
                            "/eureka/images/**",
                            "/eureka/fonts/**",
                            "/eureka/apps/**").permitAll()
                    .antMatchers(HttpMethod.POST, "/eureka/peerreplication/batch/**").permitAll()
                    .antMatchers("/info", "/health").permitAll()
                .anyRequest().hasRole("SYSTEM")
                .and().httpBasic().and().csrf().disable();
    }
}
