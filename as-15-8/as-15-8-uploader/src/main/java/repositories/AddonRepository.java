package repositories;

import model.scheme.Addon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AddonRepository extends JpaRepository<Addon, Long> {
}
