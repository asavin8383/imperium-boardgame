package handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import repositories.SystemModesRepository;

@Configuration
public class RequestHandlersConfig implements WebMvcConfigurer {

    @Autowired
    private SystemModesRepository systemModesRepository;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
                .addInterceptor(new UpdateConfigHandler(systemModesRepository))
                .addPathPatterns("*actuator/bus-refresh/**");
    }
}
