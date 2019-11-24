package repositories;

import model.traffic.CustomErdi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomErdiRepository extends JpaRepository<CustomErdi, Long> {}

