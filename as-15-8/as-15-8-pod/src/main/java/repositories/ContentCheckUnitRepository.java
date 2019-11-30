package repositories;

import model.actualViews.ContentCheckUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by san
 * Date: 03.11.2019
 */
public interface ContentCheckUnitRepository extends JpaRepository<ContentCheckUnit, Long> {

    @Query(
            "select ccu from ContentCheckUnit ccu " +
            "join Content content on ccu.contentId = content.id " +
            "and content.erdiId = :erdi_id " +
            "join ContentHistory history on content.id = history.content.id " +
            "and history.endDate = '3000-01-01'"
    )
    List<ContentCheckUnit> findAllByErdId(@Param("erdi_id") String erdiId);

}
