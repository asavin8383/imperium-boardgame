package repositories;

import model.scheme.Content;
import model.scheme.ContentResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;


@Repository
public interface ContentResourcesRepository extends JpaRepository<ContentResource, Long> {

    List<ContentResource> findByIdIn(List<Long> ids);

    List<ContentResource> findAllByContent(Content content);

    @Transactional
    void deleteByContentVersionId(Long contentVersionId);

    @Query(value =
            "SELECT * FROM sor.content_resources " +
                    "WHERE content_id = :content AND content_version_id = :version AND " +
                    "resource_type_id in (select id from sor.resource_type where dsc IN :dsc) " +
                    "limit 1", nativeQuery = true)
    ContentResource findTopByContentAndVersionAndTypeDsc(@Param("content") Long contentId,
                                                         @Param("version") Long version,
                                                         @Param("dsc") List<String> dsc);

}
