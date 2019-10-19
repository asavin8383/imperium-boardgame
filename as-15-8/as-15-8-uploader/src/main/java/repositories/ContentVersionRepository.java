package repositories;

import model.scheme.ContentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;
import java.util.List;


@Repository
public interface ContentVersionRepository extends JpaRepository<ContentVersion, Long> {

    ContentVersion findTopByIdNotNullOrderByIdDesc();

    @Transactional
    void deleteById(Long id);

}
