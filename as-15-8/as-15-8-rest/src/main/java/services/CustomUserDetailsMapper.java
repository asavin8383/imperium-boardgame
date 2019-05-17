package services;

import java.util.Collection;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import model.user.User;
import model.user.ldapUtils.UserComposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import repositories.UserRepository;

@Slf4j
@Service
public class CustomUserDetailsMapper extends LdapUserDetailsMapper {

    @Autowired
    UserRepository userRepo;

	@Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username,
                                          Collection<? extends GrantedAuthority> authorities) {
        UserDetails userDetails = super.mapUserFromContext(ctx, username, authorities);
        Attributes attributes = ctx.getAttributes();
        User user;
 
        try {
            Attribute accountName = attributes.get("samaccountname");
            user = new User(accountName.get().toString());
            Attribute firstName = attributes.get("givenname");
            if (firstName != null && firstName.size() > 0)
                user.setFirstName(firstName.get().toString());
            Attribute secondName = attributes.get("sn");
            if (secondName == null)
                secondName = attributes.get("surname");
            if (secondName != null && secondName.size() > 0)
                user.setSecondName(secondName.get().toString());
            Attribute mail = attributes.get("mail");
            if(mail!= null && mail.size()>0){
                user.setEmail(mail.get().toString());
            }
            if (!userRepo.findByUserName(user.getUserName()).isPresent()) {
                userRepo.save(user);
                log.info("User " + user.getUserName() + " was saved in DB");
            }
        } catch (NamingException e) {
            log.error("Unable to retrieve 'samaccountname', " +
                    "'givenname' (first name) or 'sn'/'surname' " +
                    "(second name) for username=" + username, e);
            user = new User(username);
        }
        return new UserComposition(user, (LdapUserDetails) userDetails);
    }
	
}
