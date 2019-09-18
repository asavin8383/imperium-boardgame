package repositories;

import model.traffic.CustomErdi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomErdiRepository extends JpaRepository<CustomErdi, Long> {

    Page<CustomErdi> findByViolationId(Pageable pageable, Long violationId);

}
