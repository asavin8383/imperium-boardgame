package repositories;

import model.DetailResultsVpn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DetailResultsVpnRepository extends JpaRepository<DetailResultsVpn, Long> {
}
