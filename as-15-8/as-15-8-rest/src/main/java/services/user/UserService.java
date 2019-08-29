package services.user;

import exceptions.AS_15_8_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.UserRepository;

/**
 * Creation date: 19.08.2019
 * Author: asavin
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class UserService {

    private final UserRepository userRepo;

    public User getUserByUserName(String userName){
        return userRepo
                .findByUserName(userName)
                .orElseThrow(() -> new AS_15_8_Exception("Пользователь с именем " + userName + " не найден в БД"));
    }
}
