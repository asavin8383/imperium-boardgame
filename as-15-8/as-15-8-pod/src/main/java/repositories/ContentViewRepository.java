package repositories;

import model.projection.ContentView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentViewRepository extends JpaRepository<ContentView, Long>, JpaSpecificationExecutor<ContentView> {

    @Query("select distinct с from ContentView c " +
            "where lower(c.id) like lower(concat('%',:query,'%')) " +
            "or lower(c.categoryName) like lower(concat('%',:query,'%')) " +
            "or lower(c.decisionOrg) like lower(concat('%',:query,'%')) " +
            "or lower(c.infoTypeId) like lower(concat('%',:query,'%'))" +
            "or lower(c.registryName) like lower(concat('%',:query,'%'))" +
            "or lower(c.resourceType) like lower(concat('%',:query,'%'))" +
            "or lower(c.resourceValue) like lower(concat('%',:query,'%'))" +
            "or lower(c.violationName) like lower(concat('%',:query,'%'))")
    Page<ContentView> findAllByQuery(@Param("query") String query, Pageable pageable);

}
