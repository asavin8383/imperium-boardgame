package repositories;

import model.scheme.ContentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ContentVersionRepository extends JpaRepository<ContentVersion, Long> {
}
