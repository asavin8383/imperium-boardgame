package repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import services.ContentService;

public interface ContentRepositoryCustom {

    Page<ContentService.ContentView> findRelevant(Pageable pageable);

}
