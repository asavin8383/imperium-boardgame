package repositories;

import model.scheme.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;


@Repository
public interface DomainRepo extends JpaRepository<Domain, Long> {

    @Query("select d.domain from Domain d " +
        "join d.domainMask dm on  dm.domainMask =:mask")
    Set<String> getDomainsByMaskId(@Param("mask") String mask);

}
