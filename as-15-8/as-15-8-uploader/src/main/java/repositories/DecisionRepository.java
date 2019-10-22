package repositories;

import model.scheme.Decision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;


@Repository
public interface DecisionRepository extends JpaRepository<Decision, Long> {

    Decision findByContent_IdAndContentVersion_Id(Long contentId, Long contentVersionId);

    @Transactional
    void deleteByContentVersionId(Long contentVersionId);
}
