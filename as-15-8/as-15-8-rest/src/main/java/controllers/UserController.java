package controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import controllers.helpers.UserHelper;
import model.user.User;
import repositories.UserRepository;

@RestController
@RequestMapping(path="/users", produces=MediaType.APPLICATION_JSON_VALUE)
public class UserController {

	@Autowired
	private UserRepository userRepo;
	
	@GetMapping(path="/current")
	public Optional<User> getSingleUser(Authentication authentication){
		return userRepo.findByUserName(UserHelper.getUserName(authentication));
	}
}
