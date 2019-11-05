package common;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import services.UserDetailsMapper;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AuthentificationManagerConfigurer extends WebSecurityConfigurerAdapter {

    @Value("${ldap.urls}")
    private String ldapUrls;

    @Value("${ldap.domain}")
    private String adDomain;

    @Value("${ldap.base.dn}")
    private String ldapBaseDn;

    private final UserDetailsMapper userDetailsMapper;

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    @Bean
    public UserDetailsService userDetailsServiceBean() throws Exception {
        return super.userDetailsServiceBean();
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

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/**").authorizeRequests()
                .antMatchers("/oauth/**")
                .permitAll()
                .anyRequest()
                .authenticated();
    }
}
