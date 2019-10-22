package repositories;

import model.scheme.PsRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;

public interface PsRepositoryCustom {

    Page<PsRecord> findByEffDtAndQuery(Date effDt, String query, Pageable pageable);

}
