package common;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class OpenApiConfiguration {

    @Value("${security.oauth2.client.accessTokenUri}")
    private String tokenUri;

    @Configuration
    public class OpenAPI30Configuration {

        @Bean
        public OpenAPI openAPI() {
            return new OpenAPI()
                    .components(getComponents())
                    .info(new Info())
                    .addSecurityItem(new SecurityRequirement().addList("Password Flow"));
        }

        private Components getComponents() {
            SecurityScheme passwordFlowScheme = new SecurityScheme()
                    .type(SecurityScheme.Type.OAUTH2)
                    .flows(new OAuthFlows()
                            .password(new OAuthFlow()
                                    .tokenUrl(tokenUri)
                                    .scopes(new Scopes().addString("openid", "profile")))
                    );

            Map<String, SecurityScheme> securitySchemes = new HashMap();
            securitySchemes.put("Password Flow", passwordFlowScheme);


            return new Components()
                    .securitySchemes(securitySchemes);
        }
    }
}
