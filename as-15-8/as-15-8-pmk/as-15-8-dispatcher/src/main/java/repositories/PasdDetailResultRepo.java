package repositories;

import model.PasdDetailResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PasdDetailResultRepo extends JpaRepository<PasdDetailResult, Long> {

}
