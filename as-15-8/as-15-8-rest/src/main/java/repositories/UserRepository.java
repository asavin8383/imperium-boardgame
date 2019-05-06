package repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import model.user.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUserName(String userName);
}
