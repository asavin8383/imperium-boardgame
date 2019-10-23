package services.ldap;

import model.enums.Role;
import model.user.User;

import java.util.List;

/**
 * Сервис получения информации с LDAP
 */

public interface LdapService {

    List<User> usersList(Role role);
}
