package repositories;

import model.traffic.CustomErdiView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomErdiViewRepository extends
        JpaRepository<CustomErdiView, Long>,
        JpaSpecificationExecutor<CustomErdiView> {
}
