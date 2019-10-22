package repositories;

import model.scheme.PasdRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;

public interface PasdRepositoryCustom {

    Page<PasdRecord> findByEffDtAndQuery(Date effDt, String query, Pageable pageable);

}
