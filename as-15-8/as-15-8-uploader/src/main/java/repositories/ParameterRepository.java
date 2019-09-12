package repositories;

import model.scheme.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface ParameterRepository extends JpaRepository<Parameter, Long> {

    @Query(value = "SELECT value FROM Parameter WHERE lower(name) = lower(?1) and enabled >= 1")
    String getEnabledParameterValue(String name);

    @Query(value = "SELECT value FROM Parameter WHERE lower(name) = lower(?1) and enabled = 0")
    String getDisabledParameterValue(String name);

    @Query(value = "SELECT value FROM Parameter WHERE lower(name) = lower(?1)")
    String getParameterValue(String name);
}
