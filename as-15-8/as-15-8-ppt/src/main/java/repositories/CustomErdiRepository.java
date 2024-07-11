package repositories;

import model.traffic.CustomErdi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface CustomErdiRepository extends JpaRepository<CustomErdi, Long> {

    Optional<CustomErdi> findByName(String name);

    @Query(value = "select ce from CustomErdi ce join ce.customErdiUnits ceu where ceu.value in :values")
    Set<CustomErdi> findAllByCustomErdiUnitsValuesIn(@Param("values") Set<String> values);

    Set<CustomErdi> findAllByNameIn(Set<String> names);

    @Query(value = "select ce.name from CustomErdi ce where ce.name in :names")
    Set<String> findNamesByNameIn(@Param("names") Set<String> names);
}
