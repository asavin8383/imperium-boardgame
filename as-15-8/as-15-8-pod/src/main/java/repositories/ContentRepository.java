package repositories;

import model.rest.control.ActCheckResultPodInfo;
import model.scheme.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import repositories.helper.DictionaryRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;


@Repository
public interface ContentRepository extends JpaRepository<Content, Long>, DictionaryRepository {

    List<Content> findByErdiIdIn(List<String> ids);

    //TODO убедиться, что includeTime не меняется от версии к версии
    @Query(
        "select new model.rest.control.ActCheckResultPodInfo(c.erdiId, ci.includeTime) from Content c " +
            "join ContentInfo ci on c.id = ci.content.id and c.id = :content_id"
    )
    Optional<ActCheckResultPodInfo> findActCheckResultPodInfo(@Param("content_id") Long contentId);

}
