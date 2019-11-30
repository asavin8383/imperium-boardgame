package repositories;

import model.traffic.SearchQueryTrafficUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchQueryTrafficUnitRepository extends JpaRepository<SearchQueryTrafficUnit, Long>,
        SearchQueryTrafficUnitRepositoryCustom {

    @Query(
            "select stu from SearchQueryTrafficUnit stu " +
            " join stu.traffic traffic " +
            " join traffic.arrangements a on a.id = :arrangement_id "
    )
    List<SearchQueryTrafficUnit> findByArrangement(@Param("arrangement_id") Long arrangementId);
}
