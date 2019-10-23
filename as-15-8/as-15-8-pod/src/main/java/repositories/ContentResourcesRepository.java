package repositories;

import model.scheme.ContentResources;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;


@Repository
public interface ContentResourcesRepository extends JpaRepository<ContentResources, Long> {

    List<ContentResources> findByIdIn(List<Long> ids);

    @Transactional
    void deleteByContentVersionId(Long contentVersionId);

    @Query(value =
            "SELECT * FROM sor.content_resources " +
                    "WHERE content_id = :content AND content_version_id = :version AND " +
                    "resource_type_id in (select id from sor.resource_type where dsc IN :dsc) " +
                    "limit 1", nativeQuery = true)
    ContentResources findTopByContentAndVersionAndTypeDsc(@Param("content") Long contentId,
                                                          @Param("version") Long version,
                                                          @Param("dsc") List<String> dsc);

}
