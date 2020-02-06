package repositories;

import model.traffic.SearchQueryPattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by san
 * Date: 06.02.2020
 */
public interface SearchQueryPatternRepoAdvanced {

    Page<SearchQueryPattern> findPage(Long id, String pattern, Pageable pageable);
}
