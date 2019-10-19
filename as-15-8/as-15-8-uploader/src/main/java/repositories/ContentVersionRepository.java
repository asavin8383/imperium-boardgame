package repositories;

import model.scheme.ContentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;
import java.util.Date;


@Repository
public interface ContentVersionRepository extends JpaRepository<ContentVersion, Long> {

    ContentVersion findTopByIdNotNullOrderByIdDesc();

    @Query(value = "SELECT regUpdateTime FROM ContentVersion WHERE lower(name) = lower(?1)")
    Date getParameterValue(String name);

    ContentVersion getTopByRegUpdateTimeNotNullOrderByIdDesc();

    @Transactional
    void deleteById(Long id);

}
