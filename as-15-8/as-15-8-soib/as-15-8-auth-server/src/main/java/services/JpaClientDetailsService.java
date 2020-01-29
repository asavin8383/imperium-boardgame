package services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Client;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Service;
import repositories.ClientsRepository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class JpaClientDetailsService implements ClientDetailsService {

    private final ClientsRepository clientsRepository;

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        Client client = clientsRepository.findClientByClientId(clientId)
                .orElseThrow(() -> new ClientRegistrationException("Ошибка! Клиент не найден: "+clientId));

        try {
            BaseClientDetails base = new BaseClientDetails(
                    client.getClientId(),
                    client.getResourceIds(),
                    client.getScopes(),
                    client.getAuthorizedGrantTypes(),
                    client.getAuthorities());
            base.setClientSecret(client.getClientSecret());
            base.setAccessTokenValiditySeconds(client.getAccessTokenValiditySeconds());
            base.setRefreshTokenValiditySeconds(client.getRefreshTokenValiditySeconds());
            if(Strings.isNotEmpty(client.getAdditionalInformation()))
                base.setAdditionalInformation(new ObjectMapper().readValue(
                        client.getAdditionalInformation(),
                        new TypeReference<HashMap<String, Object>>() {}
                    ));
            if(Strings.isNotEmpty(client.getScopes()))
                base.setAutoApproveScopes(new HashSet<>(Arrays.asList(client.getScopes().split(","))));
            return base;
        } catch(Exception ex){
            log.error("Ошибка при загрузке клиента: "+clientId, ex);
            throw new ClientRegistrationException("Ошибка при загрузке клиента: "+clientId, ex);
        }
    }
}
