package repositories;

import model.traffic.SearchQueryPattern;
import model.traffic.SearchQueryTrafficUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Created by san
 * Date: 26.11.2019
 */
@Repository
public interface SearchQueryPatternRepo extends JpaRepository<SearchQueryPattern, Long> {

    Page<SearchQueryPattern> findAllByQueryPatternContaining(String pattern, Pageable pageable);

    Page<SearchQueryPattern> findAllBySearchQueryTrafficUnits(SearchQueryTrafficUnit searchQueryTrafficUnit, Pageable pageable);

    @Query(
            "select sqp from SearchQueryPattern sqp " +
                    "join SearchQueryTrafficUnit stu " +
                    " join stu.traffic traffic " +
                    " join Arrangement a on traffic.id = a.trafficId and a.id = :arrangement_id "
    )
    List<SearchQueryPattern> findAllByArrangement(@Param("arrangement_id") Long ArrangementId);
}
