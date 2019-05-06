package controllers.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import model.user.User;
import repositories.UserRepository;
import user.UserInfo;

@Service
public class UserHelper {

	@Autowired
	UserRepository userRepo;
	
	public static String getUserName(Authentication authentication) {
		UserInfo userInfo = (UserInfo)authentication.getPrincipal();
		return userInfo.getUsername();
	}
	
	public User getOrCreateUser(Authentication authentication) {
		UserInfo userInfo = (UserInfo)authentication.getPrincipal();
		return userRepo.findByUserName(userInfo.getUsername())
				.orElseGet(() -> {
					User user = new User();
					user.setUserName(userInfo.getUsername());
					user.setFirstName(userInfo.getFirstName());
					user.setSecondName(userInfo.getSecondName());
					userRepo.save(user);
					return user;
				});
	}
	
}
