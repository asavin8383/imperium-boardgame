package com.ecls.autoconfiguration.common;

import com.ecls.autoconfiguration.endpoints.SystemModeEndpointRest;
import com.ecls.autoconfiguration.model.CurrentSystemMode;
import com.ecls.autoconfiguration.model.SystemModeRequestTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration

@AutoConfigureAfter(name = "org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration")

@ConditionalOnProperty("system.mode.autoconfig.url")
public class SystemModeConfiguration {

    @Autowired
    private Environment env;


    @Bean
    public FilterRegistrationBean<SystemModeRequestTokenFilter> loggingFilter(){
        FilterRegistrationBean<SystemModeRequestTokenFilter> registrationBean
                = new FilterRegistrationBean<>();

        registrationBean.setFilter(new SystemModeRequestTokenFilter(currentSystemMode()));
        return registrationBean;
    }


    @Bean
    public CurrentSystemMode currentSystemMode() {
        return new CurrentSystemMode(env.getProperty("system.mode.autoconfig.url"));
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public SystemModeEndpointRest systemModeEndpointRest() {
        SystemModeEndpointRest endpoint = new SystemModeEndpointRest(currentSystemMode());
        return endpoint;
    }



}
