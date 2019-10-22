package repositories;

import model.scheme.Subtype;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;

public interface SubtypeRepositoryCustom {

    Page<Subtype> findByEffDtAndQuery(Date effDt, String query, Pageable pageable);

}
