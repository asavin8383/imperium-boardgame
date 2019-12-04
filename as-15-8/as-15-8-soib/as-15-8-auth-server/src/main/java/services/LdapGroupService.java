package services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LdapGroupService {

    private final LdapTemplate ldapTemplate;
    private String base;

    @PostConstruct
    private void init(){
        base = ((LdapContextSource)ldapTemplate.getContextSource()).getBaseLdapPathAsString();
    }

    public Collection<? extends GrantedAuthority> findRoleMembers(String role){
        List<GrantedAuthority> memberRoles = new ArrayList<>();
        memberRoles.addAll(getRoleMembers(role, memberRoles));
        return memberRoles;
    }

    private List<GrantedAuthority> getRoleMembers(String role, List<GrantedAuthority> memberRoles){
        LdapQuery ldapQuery = LdapQueryBuilder
                .query()
                .where("objectClass")
                .is("group")
                .and("member").is("CN="+role+",OU=roles,"+base);

        List<GrantedAuthority> curMembers = ldapTemplate.search(ldapQuery,
                (AttributesMapper<GrantedAuthority>) attributes -> {
                    String name = attributes.get("name") == null ? "" : attributes.get("name").get().toString();
                    return new SimpleGrantedAuthority(name);
                }
        );
        for(GrantedAuthority curMember : curMembers){
            memberRoles.addAll(getRoleMembers(curMember.getAuthority(), memberRoles));
        }
        return curMembers;
    }

}
