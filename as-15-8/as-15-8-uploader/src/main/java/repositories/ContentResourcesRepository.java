package repositories;

import model.scheme.ContentResources;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;


@Repository
public interface ContentResourcesRepository extends JpaRepository<ContentResources, Long> {

    List<ContentResources> findByIdIn(List<Long> ids);

    @Transactional
    void deleteByContentVersionId(Long contentVersionId);

}
