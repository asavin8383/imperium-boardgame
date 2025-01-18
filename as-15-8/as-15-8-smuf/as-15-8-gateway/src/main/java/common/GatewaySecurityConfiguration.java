package common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
@EnableResourceServer
@EnableOAuth2Client
public class GatewaySecurityConfiguration extends ResourceServerConfigurerAdapter {
    @Override
    public void configure(final HttpSecurity http) throws Exception {
        http
                .cors().and()
                .httpBasic().disable()
                .csrf().disable()
                .authorizeRequests()
                    .antMatchers("/security/oauth/**", "/eureka/**", "/security/.well-known/jwks.json")
                    .permitAll()
                    .antMatchers(HttpMethod.GET,
                            "/pod/erdi/single/**",
                            "/pod/subtype/single_string/**",
                            "/pod/erdi/checkUnits/**",
                            "/viewer/**",
                            "/arrangements/checkUnits/**",
                            "/ppt/arrangements/checkUnits/**",
                            "/dispatcher/act/checkResult/**",
                            "/dispatcher/act/screenshots/**",
                            "/dispatcher/results/ids/**",
                            "/dispatcher/results/screenshot/**",
                            "/dispatcher/results/etalon_screenshot/**",
                            "/dispatcher/results/nmap_log/**",
                            "/dispatcher/results/protocol/screenshot/**",
                            "/dispatcher/results/protocol/etalon_screenshot/**",
                            "/dispatcher/results/protocol/nmap_log/**",
                            "/dispatcher/manual_arrangement/screenshot/**",
                            "/app/kibana/**",
                            "/pod/erdi/ids/**",
                            "/swagger/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html"
                            )
                    .permitAll()
                    .antMatchers(HttpMethod.POST,
                        "/pod/act/**",
                            "/pod/erdi/checkUnits/**",
                            "/pod/erdi/check_units_count/**",
                            "/viewer/**",
                            "/config/mode/**",
                            "/config/mode/current/**",
                            "/config/mode/any/**",
                            "/pod/erdi/ids/**"
                    ).permitAll()
                    .antMatchers(HttpMethod.OPTIONS, "/viewer/**")
                    .permitAll()
                    .antMatchers("/**")
                    .authenticated();
        http.addFilterBefore(new ForwardedHeaderFilter(), WebAsyncManagerIntegrationFilter.class);
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
