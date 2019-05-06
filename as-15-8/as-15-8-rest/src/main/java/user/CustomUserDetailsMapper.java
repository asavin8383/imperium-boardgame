package user;

import java.util.Collection;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomUserDetailsMapper extends LdapUserDetailsMapper {

	@Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username,
                                          Collection<? extends GrantedAuthority> authorities) {
        UserDetails userDetails = super.mapUserFromContext(ctx, username, authorities);
        Attributes attributes = ctx.getAttributes();
        UserInfo userInfo;
 
        try {
            Attribute accountName = attributes.get("samaccountname");
            userInfo = new UserInfo(accountName.get().toString());
            Attribute firstName = attributes.get("givenname");
            if (firstName != null && firstName.size() > 0)
                userInfo.setFirstName(firstName.get().toString());
            Attribute secondName = attributes.get("sn");
            if (secondName == null)
                secondName = attributes.get("surname");
            if (secondName != null && secondName.size() > 0)
                userInfo.setSecondName(secondName.get().toString());
        } catch (NamingException e) {
            log.error("Unable to retrieve 'samaccountname', " +
                    "'givenname' (first name) or 'sn'/'surname' " +
                    "(second name) for username=" + username, e);
            userInfo = new UserInfo(username);
        }
        return new UserComposition(userInfo, (LdapUserDetails) userDetails);
    }
	
}
