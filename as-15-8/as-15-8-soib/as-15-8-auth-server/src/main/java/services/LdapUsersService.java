package services;

import lombok.RequiredArgsConstructor;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;

import javax.naming.directory.Attribute;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LdapUsersService {

    private final LdapTemplate ldapTemplate;
    private String base;


    public List<User> getUserNamesByRole(String role){

        base = ((LdapContextSource)ldapTemplate.getContextSource()).getBaseLdapPathAsString();

        return searchRoles(createRolesQuery(role)).stream()
                .map(fRole -> searchUsers(createUsersQuery(fRole)))
                .flatMap(List::stream)
                .collect(Collectors.toList());

    }

    private List<String> searchRoles(LdapQuery ldapQuery) {
        return search("cn", ldapQuery);
    }

    private List<String> search(String param, LdapQuery ldapQuery) {
        return ldapTemplate.search(ldapQuery,
                (AttributesMapper<String>) attributes ->
                        attributes.get(param) == null ? "" : attributes.get(param).get().toString()
        );
    }

    private List<User> searchUsers(LdapQuery ldapQuery) {
        return ldapTemplate.search(ldapQuery,
                (AttributesMapper<User>) attributes -> {
                    User user = new User();
                    Attribute name = attributes.get("name");
                    if (name != null && name.size() > 0)
                        user.setName(name.get().toString());
                    Attribute firstName = attributes.get("givenname");
                    if (firstName != null && firstName.size() > 0)
                        user.setFirstName(firstName.get().toString());
                    Attribute secondName = attributes.get("sn");
                    if (secondName == null)
                        secondName = attributes.get("surname");
                    if (secondName != null && secondName.size() > 0)
                        user.setLastName(secondName.get().toString());
                    Attribute email = attributes.get("mail");
                    if(email != null && email.size()>0)
                        user.setMail(email.get().toString());
                    return user;
                });
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
