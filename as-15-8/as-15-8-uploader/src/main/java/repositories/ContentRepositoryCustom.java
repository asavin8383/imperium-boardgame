package repositories;

import model.projection.ContentView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;

public interface ContentRepositoryCustom {

    Page<ContentView> findByEffDtAndQuery(Date effDt, String query, Pageable pageable);

}
