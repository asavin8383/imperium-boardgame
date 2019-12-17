package repositories;

import enums.ErdiStatus;
import model.scheme.ContentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;


@Repository
public interface ContentHistoryRepository extends JpaRepository<ContentHistory, Long> {

    @Query("SELECT hist FROM ContentHistory hist WHERE hist.content.id = :contentHistoryId "
            + "ORDER BY hist.contentVersion.id desc, hist.addonVersion.id desc")
    List<ContentHistory> getAllContentHistories(@Param("contentHistoryId") Long contentId);

    @Transactional
    void deleteByContentVersionId(Long contentVersionId);

    @Query("select " +
                "case when " +
                "max(h.endDate) <> '3000-01-01' " +
                "then 'EXCLUDED' " +
                "else " +
                    "case when " +
                    "max(i.includeTime) < :restrictionDate " +
                    "then 'ACTIVE' " +
                    "else 'INACTIVE' " +
                    "end " +
                "end " +
            "from ContentHistory h " +
            "join ContentInfo i " +
            "on h.content = i.content " +
            "and h.content.id = :contentId " +
            "group by h.content.id")
    ErdiStatus checkErdiStatus(@Param("contentId") Long contentId, @Param("restrictionDate") Date restrictionDate);

}
