package repositories;

import model.sor.FormalErdi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import repositories.helpers.FormalErdiParams;

public interface FormalErdiRepositoryCustom {

    Page<FormalErdi> searchFor(Class<FormalErdi> clazz, FormalErdiParams params, Pageable pageable);

    Page<FormalErdi> findRelevant(String entryTypeId, String urgencyTypeId, String blockType,
                                  String value, Integer resourceTypeId, Pageable pageable);

}
