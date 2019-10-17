package repositories;

import model.traffic.CustomErdi;
import model.traffic.projection.CustomErdiRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import repositories.helpers.CustomErdiParams;

@Repository
public interface CustomErdiRepositoryCustom {

    Page<CustomErdi> searchFor(Class<CustomErdi> clazz, CustomErdiParams params, Pageable pageable);

    Page<CustomErdiRow> searchFor(CustomErdiParams params, Pageable pageable);

}
