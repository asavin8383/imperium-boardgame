package services.userDetails;

import exceptions.AS_15_8_Exception;
import lombok.extern.slf4j.Slf4j;
import model.enums.Role;
import model.user.User;
import model.user.UserRole;
import model.user.ldapUtils.UserComposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.stereotype.Service;
import repositories.UserRepository;
import repositories.UserRoleRepository;

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
public class CustomUserDetailsMapper extends LdapUserDetailsMapper {

    private UserRepository userRepo;

    private UserRoleRepository userRoleRepo;

    @Autowired
    public CustomUserDetailsMapper(UserRepository userRepo, UserRoleRepository userRoleRepo) {
        this.userRepo = userRepo;
        this.userRoleRepo = userRoleRepo;
    }

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username,
                                          Collection<? extends GrantedAuthority> authorities) {
        UserDetails userDetails = super.mapUserFromContext(ctx, username, authorities);
        Attributes attributes = ctx.getAttributes();
        User user;
        try {
            user = userRepo.findByUserName(attributes.get("samaccountname").get().toString())
            .orElseGet(() -> {
                Attribute accountName = attributes.get("samaccountname");
                User newUser = null;
                try {
                    newUser = new User(accountName.get().toString());
                Attribute firstName = attributes.get("givenname");
                if (firstName != null && firstName.size() > 0)
                    newUser.setFirstName(firstName.get().toString());
                Attribute secondName = attributes.get("sn");
                if (secondName == null)
                    secondName = attributes.get("surname");
                if (secondName != null && secondName.size() > 0)
                    newUser.setSecondName(secondName.get().toString());
                Attribute mail = attributes.get("mail");
                if(mail != null && mail.size()>0){
                    newUser.setEmail(mail.get().toString());
                }
                if(authorities != null && authorities.size()>0) {
                    for(GrantedAuthority authority : authorities){
                        try {
                            Role role = Role.valueOf(authority.getAuthority());
                            UserRole userRole = userRoleRepo.findByRole(role)
                                    .orElseGet(() -> {
                                        UserRole newUserRole = new UserRole();
                                        newUserRole.setRole(role);
                                        return userRoleRepo.save(newUserRole);
                                    });
                            newUser.getRoles().add(userRole);
                        } catch (IllegalArgumentException ex){
                            throw new AS_15_8_Exception("Error parsing user role from LDAP! Role is not supported: " + authority.getAuthority());
                        }
                    }
                }
                userRepo.save(newUser);
                log.info("User " + newUser.getUserName() + " was saved in DB");
                return newUser;
                } catch (NamingException ex) {
                    throw new AS_15_8_Exception("Error mapping user: " + newUser, ex);
                }
            });
        } catch (Exception ex){
            throw new AS_15_8_Exception("Error mapping user from LDAP ", ex);
        }
        return new UserComposition(user, (LdapUserDetails) userDetails);
    }

}
