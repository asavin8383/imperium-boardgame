package repositories;

import model.scheme.ContentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;


@Repository
public interface ContentInfoRepository extends JpaRepository<ContentInfo, Long> {

    ContentInfo findTopByContentVersionIdNotNullOrderByContentVersionIdDesc();

    @Transactional
    void deleteByContentVersionId(Long contentVersionId);


}
