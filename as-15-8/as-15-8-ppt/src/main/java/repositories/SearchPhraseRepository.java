package repositories;

import model.traffic.SearchPhrase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchPhraseRepository extends JpaRepository<SearchPhrase, Long>, SearchPhraseRepositoryCustom {

    @Query(value = "select exists(select 1 from portal.search_query_traffic_units_search_phrases " +
            "where search_phrase_id = :searchPhraseId AND traffic_unit_id = :trafficUnitId)",
            nativeQuery = true)
    boolean belongsToSearchTrafficUnit(@Param("trafficUnitId") Long trafficUnitId,
                                       @Param("searchPhraseId") Long searchPhraseId);

}
