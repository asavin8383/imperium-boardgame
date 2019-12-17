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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LdapUsersService {

    private final LdapTemplate ldapTemplate;
    private String base;


    public List<String> getUserNamesByRole(String role){

        base = ((LdapContextSource)ldapTemplate.getContextSource()).getBaseLdapPathAsString();

        return searchRoles(createRolesQuery(role)).stream()
                .map(fRole -> searchUserNames(createUsersQuery(fRole)))
                .flatMap(List::stream)
                .collect(Collectors.toList());

    }

    private List<String> searchRoles(LdapQuery ldapQuery) {
        return search("cn", ldapQuery);
    }

    private List<String> searchUserNames(LdapQuery ldapQuery) {
        return search("displayname", ldapQuery);
    }

    private List<String> search(String param, LdapQuery ldapQuery) {
        return ldapTemplate.search(ldapQuery,
                (AttributesMapper<String>) attributes ->
                        attributes.get(param) == null ? "" : attributes.get(param).get().toString()
        );
    }

    private LdapQuery createUsersQuery(String role) {
        return LdapQueryBuilder
                .query()
                .where("objectClass")
                .is("user")
                .and("memberOf").is("CN=" + role + ",OU=roles," + base);
    }

    private LdapQuery  createRolesQuery(String role) {
        return LdapQueryBuilder
                .query()
                .where("objectClass")
                .is("group")
                .and("memberOf").is("CN=" + role + ",OU=functional_roles,OU=roles," + base);
    }

}
