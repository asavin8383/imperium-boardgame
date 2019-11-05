package jwt;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import user.UserDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UserTokenEnhancer implements org.springframework.security.oauth2.provider.token.TokenEnhancer {
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        final Map<String, Object> additionalInfo = new HashMap<>();
        if(authentication.getUserAuthentication() != null &&
                authentication.getUserAuthentication().getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getUserAuthentication().getPrincipal();
            additionalInfo.put("userName", userDetails.getUsername());
            additionalInfo.put("firstName", userDetails.getFirstName());
            additionalInfo.put("secondName", userDetails.getLastName());
            additionalInfo.put("mail", userDetails.getMail());
        }

        additionalInfo.put("role", authentication.getAuthorities()
                .stream()
                .map(at -> at.getAuthority())
                .collect(Collectors.joining(",")));
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        return accessToken;
    }
}
