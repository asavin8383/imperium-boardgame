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
public interface ContentViewRepository extends JpaRepository<ContentView, String>, ContentViewRepositoryAdvanced, JpaSpecificationExecutor<ContentView> {

    @Query("select DISTINCT(c.resourceType) from ContentView c")
    List<String> getDistinctResourceTypes();

    @Query("select DISTINCT(c.categoryName) from ContentView c")
    List<String> getDistinctCategoryNames();

    @Query("select DISTINCT(c.decisionOrg) from ContentView c")
    List<String> getDistinctDecisionOrgs();

    @Query("select DISTINCT(c.infoTypeId) from ContentView c")
    List<String> getDistinctInfoTypeIds();

    @Query("select DISTINCT(c.registryName) from ContentView c")
    List<String> getDistinctRegistryNames();

    @Query("select DISTINCT(c.violationName) from ContentView c")
    List<String> getDistinctViolationNames();

}
