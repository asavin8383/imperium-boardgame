package repositories;

import model.ErrorDetailResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ErrorDetailResultRepo extends JpaRepository<ErrorDetailResult, Long> {
}
