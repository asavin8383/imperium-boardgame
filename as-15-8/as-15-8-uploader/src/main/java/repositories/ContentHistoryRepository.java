package repositories;

import model.scheme.ContentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;
import java.util.List;


@Repository
public interface ContentHistoryRepository extends JpaRepository<ContentHistory, Long> {

    @Query("SELECT hist FROM ContentHistory hist WHERE hist.content.id = :contentHistoryId "
            + "ORDER BY hist.contentVersion.id desc, hist.addonVersion.id desc")
    List<ContentHistory> getAllContentHistories(@Param("contentHistoryId") Long contentId);

    @Transactional
    void deleteByContentVersionId(Long contentVersionId);

}
