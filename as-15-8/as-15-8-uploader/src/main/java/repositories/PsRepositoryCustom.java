package repositories;

import model.scheme.PsRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface PsRepositoryCustom {

    Page<PsRecord> findByEffDtAndQuery(LocalDateTime effDt, String query, Pageable pageable);

}
