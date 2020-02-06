package repositories;

import model.traffic.SearchQueryPattern;
import model.traffic.SearchQueryPatternContentJoin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by san
 * Date: 04.12.2019
 */
@Repository
public interface SearchQueryPatternContentJoinRepo extends JpaRepository<SearchQueryPatternContentJoin, Long> {

    Page<SearchQueryPatternContentJoin> findAllBySearchQueryPattern(SearchQueryPattern searchQueryPattern, Pageable pageable);

    List<SearchQueryPatternContentJoin> findAllBySearchQueryPatternAndContentIdIn(SearchQueryPattern searchQueryPattern, List<Long> ids);
}
