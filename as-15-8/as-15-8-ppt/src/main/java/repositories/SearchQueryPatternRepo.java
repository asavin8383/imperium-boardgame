package repositories;

import model.traffic.SearchQueryPattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Created by san
 * Date: 26.11.2019
 */
@Repository
public interface SearchQueryPatternRepo extends JpaRepository<SearchQueryPattern, Long> {

    Page<SearchQueryPattern> findAllByQueryPatternContaining(String pattern, Pageable pageable);
}
