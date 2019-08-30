package repositories;

import model.scheme.ContentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ContentInfoRepository extends JpaRepository<ContentInfo, Long> {
}
