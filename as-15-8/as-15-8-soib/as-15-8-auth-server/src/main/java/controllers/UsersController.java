package controllers;

import lombok.RequiredArgsConstructor;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import services.LdapUsersService;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UsersController {

    private final LdapUsersService usersService;

    @GetMapping("/operator")
    @PreAuthorize("hasAnyRole('ROLE_MANAGE_FORMAL_TASK','ROLE_SYSTEM')")
    public List<User> getOperators(String role) {
        return usersService.getUserNamesByRole("ROLE_FORMAL_TASK");
    }

}
