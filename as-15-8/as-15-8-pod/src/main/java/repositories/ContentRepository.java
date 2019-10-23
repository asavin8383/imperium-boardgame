package repositories;

import model.scheme.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import repositories.helper.DictionaryRepository;

import javax.transaction.Transactional;
import java.util.List;


@Repository
public interface ContentRepository extends
        JpaRepository<Content, Long>,
        ContentRepositoryCustom,
        DictionaryRepository {

    List<Content> findByErdiIdIn(List<String> ids);

    List<Content> findByIdIn(List<Long> ids);

    @Transactional
    void deleteByContentVersionId(Long contentVersionId);

}
