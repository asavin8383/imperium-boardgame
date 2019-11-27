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

    @Query("select distinct c from ContentView c " +
            "where (concat(c.id, '') like lower(concat('%',:query,'%')) " +
            "or lower(c.categoryName) like lower(concat('%',:query,'%')) " +
            "or lower(c.decisionOrg) like lower(concat('%',:query,'%')) " +
            "or lower(c.infoTypeId) like lower(concat('%',:query,'%')) " +
            "or lower(c.registryName) like lower(concat('%',:query,'%')) " +
            "or lower(c.resourceType) like lower(concat('%',:query,'%')) " +
            "or lower(c.resourceValue) like lower(concat('%',:query,'%')) " +
            "or lower(c.violationName) like lower(concat('%',:query,'%'))) " +
            "and concat(c.id, '') like lower(concat('%',:id,'%')) " +
            "and lower(c.categoryName) like lower(concat('%',:categoryName,'%')) " +
            "and lower(c.decisionOrg) like lower(concat('%',:decisionOrg,'%')) " +
            "and lower(c.infoTypeId) like lower(concat('%',:infoTypeId,'%')) " +
            "and lower(c.registryName) like lower(concat('%',:registryName,'%')) " +
            "and ((:resourceType is not null and lower(c.resourceType) in (:resourceType)) or true) " +
            "and lower(c.resourceValue) like lower(concat('%',:resourceValue,'%')) " +
            "and lower(c.violationName) like lower(concat('%',:violationName,'%')) "
    )
    Page<ContentView> findAllByQuery(@Param("query") String query,
                                     @Param("id") String id,
                                     @Param("categoryName") String categoryName,
                                     @Param("decisionOrg") String decisionOrg,
                                     @Param("infoTypeId") String infoTypeId,
                                     @Param("registryName") String registryName,
                                     @Param("resourceType") List<String> resourceType,
                                     @Param("resourceValue") String resourceValue,
                                     @Param("violationName") String violationName,
                                     Pageable pageable);

}
