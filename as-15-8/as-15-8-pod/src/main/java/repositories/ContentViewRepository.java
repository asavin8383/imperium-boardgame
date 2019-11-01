package repositories;

import model.projection.ContentView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentViewRepository extends JpaRepository<ContentView, Long>, JpaSpecificationExecutor<ContentView> {
}
