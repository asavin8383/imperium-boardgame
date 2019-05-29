package services.ldap.impl;

import exceptions.AS_15_8_Exception;
import lombok.extern.slf4j.Slf4j;
import model.enums.Role;
import model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;
import services.ldap.LdapService;
import utils.UserAttributesMapper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LdapServiceImpl implements LdapService {

    @Value("${ldap.login.filter_oper}")
    private String operFilter;

    @Value("${ldap.login.filter_admin}")
    private String adminFilter;

    @Autowired
    private LdapTemplate ldapTemplate;

    @Override
    public List<User> usersList(Role role) {
        String filter;
        if(role == null){
            filter = "(objectClass=user)";
        }else if (role.equals(Role.ROLE_ADMIN)){
            filter = adminFilter;
        }else if (role.equals(Role.ROLE_OPERATOR)){
            filter = operFilter;
        } else {
            throw new AS_15_8_Exception("Error getting user list from LDAP! Role doesn't supported: " + role.toString());
        }
        List<User> users = new ArrayList<>();
        try {
            List<User> search = ldapTemplate.search("", filter, new UserAttributesMapper());
            users.addAll(search);
        } catch (Exception ex) {
            log.error("Error: " + ex);
            throw new AS_15_8_Exception("Error getting user list from LDAP!", ex);
        }
        return users;
    }
}
