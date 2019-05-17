package controllers.helpers;

import model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import repositories.UserRepository;

@Service
public class UserHelper {

	@Autowired
	UserRepository userRepo;
	
	public static String getUserName(Authentication authentication) {
		User user = (User)authentication.getPrincipal();
		return user.getUserName();
	}
	
	public User getOrCreateUser(Authentication authentication) {
		User user = (User)authentication.getPrincipal();
		return userRepo.findByUserName(user.getUserName())
				.orElseGet(() -> {
					User newUser = new User();
					newUser.setUserName(user.getUserName());
					newUser.setFirstName(user.getFirstName());
					newUser.setSecondName(user.getSecondName());
					userRepo.save(newUser);
					return newUser;
				});
	}
	
}
