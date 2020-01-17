package repositories;

import model.GlobalProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalPropertiesRepository extends JpaRepository<GlobalProperty, Long> {

    public String getGlobalPropertyByKey(String key);
}
