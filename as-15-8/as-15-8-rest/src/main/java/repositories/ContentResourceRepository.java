package repositories;

import model.sor.ContentResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentResourceRepository extends JpaRepository<ContentResource, Long> {

    @Query(value =
            "select * from sor.content_resources " +
            "where resource_type_id in (select id from sor.resource_type " +
                                        "where dsc like :description) " +
            "limit 1",
            nativeQuery = true)
    ContentResource findOneByDescription(@Param("description") String description);

}
