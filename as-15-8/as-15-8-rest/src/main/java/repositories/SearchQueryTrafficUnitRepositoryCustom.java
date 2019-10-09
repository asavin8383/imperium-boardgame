package repositories;

import model.traffic.SearchQueryTrafficUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import repositories.helpers.SearchTemplateParams;

public interface SearchQueryTrafficUnitRepositoryCustom {

    Page<SearchQueryTrafficUnit> searchFor(Class<SearchQueryTrafficUnit> clazz, SearchTemplateParams params, Pageable pageable);
}
