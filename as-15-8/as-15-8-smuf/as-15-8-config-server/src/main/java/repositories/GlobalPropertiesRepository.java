package repositories;

import model.GlobalProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface GlobalPropertiesRepository extends JpaRepository<GlobalProperty, Long> {

    @Query(value = "select g.value from GlobalProperty g where g.key = :keyVal")
    String getGlobalPropertyByKey(@Param("keyVal") String key);
}
