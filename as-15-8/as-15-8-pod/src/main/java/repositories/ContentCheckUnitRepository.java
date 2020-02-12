package repositories;

import checkUnits.CheckUnitType;
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
    List<ContentCheckUnit> findAllByErdId(@Param("erdi_id") Long erdiId);


    @Query(
            "select ccu from ContentCheckUnit ccu " +
                    "join Content content on ccu.contentId = content.id " +
                    "and content.erdiId in(:erdi_ids) " +
                    "join ContentHistory history on content.id = history.content.id " +
                    "and history.endDate = '3000-01-01'"
    )
    List<ContentCheckUnit> findAllByErdIds(@Param("erdi_ids") List<Long> erdiIds);

    @Query(
            "select count(ccu) from ContentCheckUnit ccu " +
                    "join Content content on ccu.contentId = content.id " +
                    "and content.erdiId in(:erdi_ids) " +
                    "join ContentHistory history on content.id = history.content.id " +
                    "and history.endDate = '3000-01-01' " +
                    "and ccu.checkUnitType<>:exclude "
    )

    Long findAllByErdIdsExclude(@Param("erdi_ids") List<Long> erdiIds, @Param("exclude") CheckUnitType checkUnitType);

    @Query("select ccu.checkUnitValue from ContentCheckUnit ccu " +
            "join Content content on ccu.contentId = content.id " +
            "and content.erdiId in(:erdi_ids) " +
            "join ContentHistory history on content.id = history.content.id " +
            "and history.endDate = '3000-01-01' " +
            "and ccu.checkUnitType=:checkUnitType")

    List<String> findAllCheckUnitsValueBy(@Param("erdi_ids") List<Long> erdiIds, @Param("checkUnitType") CheckUnitType checkUnitType);
}
