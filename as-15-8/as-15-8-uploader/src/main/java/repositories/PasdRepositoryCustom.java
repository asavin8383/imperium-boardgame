package repositories;

import model.scheme.PasdRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface PasdRepositoryCustom {

    Page<PasdRecord> findByEffDtAndQuery(LocalDateTime effDt, String query, Pageable pageable);

}
