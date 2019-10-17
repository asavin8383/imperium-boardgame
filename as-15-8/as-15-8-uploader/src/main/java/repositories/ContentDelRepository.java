package repositories;

import model.scheme.ContentDel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface ContentDelRepository extends JpaRepository<ContentDel, Long> {


}
