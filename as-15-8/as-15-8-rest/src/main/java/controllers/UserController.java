package controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import model.enums.Role;
import model.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import controllers.helpers.UserHelper;
import model.user.User;
import repositories.UserRepository;
import services.ldap.LdapService;

@RestController
@RequestMapping(path="/users", produces=MediaType.APPLICATION_JSON_VALUE)
public class UserController {

	private UserRepository userRepo;
	private LdapService ldapService;

	@Autowired
	public UserController(UserRepository userRepo, LdapService ldapService) {
		this.userRepo = userRepo;
		this.ldapService = ldapService;
	}

	@GetMapping(path="/current")
	public Optional<User> getSingleUser(Authentication authentication){
		return userRepo.findByUserName(UserHelper.getUserName(authentication));
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
	@GetMapping(path="/operators")
	public List<User> findAllOperators(){
		return userRepo.findByRoles_Role(Role.ROLE_OPERATOR);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping(path="/operators/ldap")
	public List<User> operatorList(){
		return ldapService.usersList(Role.ROLE_OPERATOR);
	}

}
