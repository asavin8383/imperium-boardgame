package repositories;

import model.scheme.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface ParameterRepository extends JpaRepository<Parameter, Long> {

    @Query(value = "SELECT value FROM Parameter WHERE lower(name) = lower(?1)")
    String getParameterValue(String name);

    Parameter findByName(String name);

    @Modifying(clearAutomatically = true)
    @Query("update Parameter p set p.value =:value where p.name =:name")
    void updateParameter(@Param("name") String name, @Param("value") String value);
}
