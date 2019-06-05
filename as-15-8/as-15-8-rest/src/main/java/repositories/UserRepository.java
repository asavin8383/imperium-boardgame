package repositories;

import java.util.List;
import java.util.Optional;

import model.enums.Role;
import model.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import model.user.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUserName(String userName);

	List<User> findByRoles_Role(Role role);
}
