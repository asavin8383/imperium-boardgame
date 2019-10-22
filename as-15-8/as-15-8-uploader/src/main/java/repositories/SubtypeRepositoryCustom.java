package repositories;

import model.scheme.Subtype;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface SubtypeRepositoryCustom {

    Page<Subtype> findByEffDtAndQuery(LocalDateTime effDt, String query, Pageable pageable);

}
