package com.ecls.autoconfiguration.common;

import com.ecls.autoconfiguration.endpoints.SystemModeEndpointRest;
import com.ecls.autoconfiguration.exceptions.AS_15_8_System_Mode_Exception;
import com.ecls.autoconfiguration.model.CurrentSystemMode;
import com.ecls.autoconfiguration.model.SystemModeRequestTokenFilter;
import enums.SystemModeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration

@AutoConfigureAfter(name = "org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration")
//@ConditionalOnProperty(name = "system.mode.control.enabled", havingValue = "true")
@ConditionalOnProperty("system.mode.autoconfig.url")
//@ConditionalOnBean(name = "dispatcherServlet")
public class SystemModeConfiguration {

    @Autowired
    private Environment env;

    //private final String CURRENT_SYSTEM_MODE_URI = "/config/mode";


    //@ConditionalOnBean(name = "eurekaAutoServiceRegistration")
    //@Bean


    /*private String createUri(String url) {
        return UriComponentsBuilder
                *//*.fromHttpUrl(env.getProperty("gateway.url"))
                .path(CURRENT_SYSTEM_MODE_URI)*//*
                .fromHttpUrl(url)
                .build().toString();
    }*/

    @Bean
    public FilterRegistrationBean<SystemModeRequestTokenFilter> loggingFilter(){
        FilterRegistrationBean<SystemModeRequestTokenFilter> registrationBean
                = new FilterRegistrationBean<>();

        registrationBean.setFilter(new SystemModeRequestTokenFilter(currentSystemMode()));
        return registrationBean;
    }


    @Bean
    @ConditionalOnEnabledEndpoint
    public SystemModeEndpointRest systemModeEndpointRest() {
        SystemModeEndpointRest endpoint = new SystemModeEndpointRest(currentSystemMode());
        return endpoint;
    }

    @Bean
    public CurrentSystemMode currentSystemMode() {
        return new CurrentSystemMode(env.getProperty("system.mode.autoconfig.url"));
    }

}
