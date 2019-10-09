package repositories;

import model.erdi.FormalErdi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import repositories.helpers.FormalErdiParams;

public interface FormalErdiRepositoryCustom {

    Page<FormalErdi> searchFor(Class<FormalErdi> clazz, FormalErdiParams params, Pageable pageable);

}
