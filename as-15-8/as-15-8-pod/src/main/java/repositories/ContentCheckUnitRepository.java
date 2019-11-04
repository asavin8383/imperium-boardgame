package repositories;

import model.actualViews.ContentCheckUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by san
 * Date: 03.11.2019
 */
public interface ContentCheckUnitRepository extends JpaRepository<ContentCheckUnit, Long> {

    List<ContentCheckUnit> findAllByContentId(Long contentId);

}
