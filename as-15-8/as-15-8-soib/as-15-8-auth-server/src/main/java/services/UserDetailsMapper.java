package services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.stereotype.Service;
import user.UserDetails;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.Collection;

/**
 * Класс-mapper пользователя LDAP на пользователя БД
 * @author asavin
 */

@Slf4j
@Service
public class UserDetailsMapper extends LdapUserDetailsMapper {

    @Override
    public UserDetails mapUserFromContext(
            DirContextOperations ctx, String username,
            Collection<? extends GrantedAuthority> authorities) {

        LdapUserDetails userDetails = (LdapUserDetails) super.mapUserFromContext(ctx, username, authorities);
        Attributes attributes = ctx.getAttributes();

        String firstName ="";
        String lastName = "";
        String mail = "";
        try {
            Attribute name = attributes.get("givenname");
            if (name != null && name.size() > 0)
                firstName = name.get().toString();
            Attribute secondName = attributes.get("sn");
            if (secondName == null)
                secondName = attributes.get("surname");
            if (secondName != null && secondName.size() > 0)
                lastName = secondName.get().toString();
            Attribute email = attributes.get("mail");
            if(email != null && email.size()>0)
                mail = email.get().toString();
        } catch(NamingException ex){
            throw new RuntimeException("Error mapping user: " + username, ex);
        }
        return new UserDetails(username, firstName, lastName, mail, userDetails);
    }

}
