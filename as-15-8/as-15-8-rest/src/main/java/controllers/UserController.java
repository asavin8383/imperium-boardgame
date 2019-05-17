package controllers;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import model.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import controllers.helpers.UserHelper;
import model.user.User;
import repositories.UserRepository;
import services.LdapService;

@RestController
@RequestMapping(path="/users", produces=MediaType.APPLICATION_JSON_VALUE)
public class UserController {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private LdapService ldapService;
	
	@GetMapping(path="/current")
	public Optional<User> getSingleUser(Authentication authentication){
		return userRepo.findByUserName(UserHelper.getUserName(authentication));
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping(path="/operators/ldap")
	public List<User> operatorList(){
		return ldapService.usersList(Role.ROLE_OPERATOR);
	}

}
