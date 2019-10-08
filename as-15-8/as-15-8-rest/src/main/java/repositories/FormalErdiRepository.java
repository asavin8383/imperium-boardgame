package repositories;

import model.erdi.FormalErdi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormalErdiRepository extends JpaRepository<FormalErdi, Long>, FormalErdiRepositoryCustom {

}
