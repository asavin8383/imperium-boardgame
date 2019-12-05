package common;

import model.soap.SoapOperation;
import org.eclipse.birt.report.filter.ViewerFilter;
import org.eclipse.birt.report.servlet.BirtEngineServlet;
import org.eclipse.birt.report.servlet.ViewerServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import java.util.HashMap;
import java.util.Map;

/**
 * User: asinjavin
 * Date: 21.11.2019
 * Time: 22:47
 */
@Configuration
public class ServletConfiguration implements WebMvcConfigurer
{
    private Servlet viewerServlet = new ViewerServlet();
    private Servlet engineServlet = new BirtEngineServlet();
    private Filter viewerFilter = new ViewerFilter();

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Map<String, SoapOperation> requests() {
        return new HashMap<>();
    }

    @Bean
    public ServletRegistrationBean registerFrameset() {
        return new ServletRegistrationBean<>(viewerServlet, "/frameset");
    }

    @Bean
    public ServletRegistrationBean registerRun() {
        return new ServletRegistrationBean<>(viewerServlet, "/run");
    }

    @Bean
    public ServletRegistrationBean register_preview() {
        return new ServletRegistrationBean<>(engineServlet, "/preview");
    }

    @Bean
    public ServletRegistrationBean register_download() {
        return new ServletRegistrationBean<>(engineServlet, "/download");
    }

    @Bean
    public ServletRegistrationBean register_parameter() {
        return new ServletRegistrationBean<>(engineServlet, "/parameter");
    }

    @Bean
    public ServletRegistrationBean register_document() {
        return new ServletRegistrationBean<>(engineServlet, "/document");
    }

    @Bean
    public ServletRegistrationBean register_output() {
        return new ServletRegistrationBean<>(engineServlet, "/output");
    }

    @Bean
    public ServletRegistrationBean register_extract() {
        return new ServletRegistrationBean<>(engineServlet, "/extract");
    }

//    @Bean
//    public FilterRegistrationBean loggingFilter() {
//        FilterRegistrationBean<ViewerFilter> registrationBean = new FilterRegistrationBean<>();
//
//        registrationBean.setFilter(new ViewerFilter());
//        registrationBean.addServletNames("ViewerServlet", "BirtEngineServlet");
//
//        return registrationBean;
//    }
//
    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
		"classpath:/META-INF/resources/", "classpath:/resources/",
		"classpath:/static/", "classpath:/public/" };

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("registry = " + registry);
        if (!registry.hasMappingForPattern("/birt/**")) {
            registry.addResourceHandler("/birt/**").addResourceLocations("classpath:/birt/");
        }
        if (!registry.hasMappingForPattern("/**")) {
            registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
        }
    }
}
