package services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LdapUsersService {

    private final LdapTemplate ldapTemplate;



    public List<String> getUserNamesByRole(String role){
        final String base = ((LdapContextSource)ldapTemplate.getContextSource()).getBaseLdapPathAsString();

        LdapQuery ldapQuery = LdapQueryBuilder
                .query()
                .where("objectClass")
                .is("user")
                .and("memberOf").is("CN="+role+",OU=roles,"+base);

        return ldapTemplate.search(ldapQuery,
                (AttributesMapper<String>) attributes ->
                        attributes.get("displayname") == null ? "" : attributes.get("displayname").get().toString()
        );
    }

}
