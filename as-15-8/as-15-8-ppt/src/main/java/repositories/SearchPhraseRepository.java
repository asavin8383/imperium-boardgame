package repositories;

import model.traffic.SearchPhrase;
import model.traffic.SearchQueryPattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchPhraseRepository extends JpaRepository<SearchPhrase, Long> {

    Page<SearchPhrase> findAllBySearchQueryPatternsAndPhraseContaining(SearchQueryPattern searchQueryPattern, String phrase, Pageable pageable);

    Page<SearchPhrase> findAllByPhraseContaining(String phrase, Pageable pageable);

}
