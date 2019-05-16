package utils;

import model.user.User;
import org.springframework.ldap.core.AttributesMapper;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

public class UserAttributesMapper implements AttributesMapper {
    @Override
    public Object mapFromAttributes(Attributes attributes) throws NamingException {
        User user = new User();

        Attribute samaccountname = attributes.get("samaccountname");
        if (samaccountname != null){
            user.setUserName((String) samaccountname.get());
        }

        Attribute givenname = attributes.get("givenname");
        if (givenname != null){
            user.setFirstName((String) givenname.get());
        }

        return user;
    }
}
