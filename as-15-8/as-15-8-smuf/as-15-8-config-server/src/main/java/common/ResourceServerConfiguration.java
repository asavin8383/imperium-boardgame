package common;

import filters.UpdateConfigFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import repositories.SystemModesRepository;

import java.util.Collections;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    private final SystemModesRepository systemModesRepository;

    @Override
    public void configure(final HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/actuator/bus-refresh/**").hasRole("MANAGE_CONFIGURATIONS")
                .antMatchers("/robots/**").hasRole("MANAGE_CONFIGURATIONS")
                .antMatchers(HttpMethod.GET, "/**").hasRole("CONFIG_CLIENT")
                .antMatchers(HttpMethod.POST,
                        "/mode/**",
                        "/mode/current/**",
                        "/mode/any/**").permitAll()
            .anyRequest().authenticated()
        .and().httpBasic().disable().csrf().disable();
    }

    @Bean
    public FilterRegistrationBean<UpdateConfigFilter> updateFilter(){
        FilterRegistrationBean<UpdateConfigFilter> registrationBean
                = new FilterRegistrationBean<>();

        registrationBean.setFilter(new UpdateConfigFilter(systemModesRepository));
        //registrationBean.setUrlPatterns(Collections.singletonList("/actuator/bus-refresh/"));

        return registrationBean;
    }
}
