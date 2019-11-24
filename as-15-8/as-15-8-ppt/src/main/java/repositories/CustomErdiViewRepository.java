package repositories;

import model.traffic.CustomErdiView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomErdiViewRepository extends
        JpaRepository<CustomErdiView, Long>,
        JpaSpecificationExecutor<CustomErdiView> {


    @Query("select view from CustomErdiView view " +
            " join view.erdiTrafficUnits units on units.id = :unit_id " +
            "where concat(view.id, '') like lower(concat('%',:query,'%')) " +
            "or lower(view.name) like lower(concat('%',:query,'%')) " +
            "or lower(view.unitType) like lower(concat('%',:query,'%')) " +
            "or lower(view.unitValue) like lower(concat('%',:query,'%')) " +
            "or lower(view.subtypeId) like lower(concat('%',:query,'%')) "
    )
    Page<CustomErdiView> findAllByErdiTrafficUnitsContainingAndQuery(@Param("unit_id") Long erdiTrafficUnitId, @Param("query") String query, Pageable pageable);

    @Query("select view from CustomErdiView view " +
            " join view.searchQueryTrafficUnits units on units.id = :unit_id " +
            "where concat(view.id, '') like lower(concat('%',:query,'%')) " +
            "or lower(view.name) like lower(concat('%',:query,'%')) " +
            "or lower(view.unitType) like lower(concat('%',:query,'%')) " +
            "or lower(view.unitValue) like lower(concat('%',:query,'%')) " +
            "or lower(view.subtypeId) like lower(concat('%',:query,'%')) "
    )
    Page<CustomErdiView> findAllBySearchQueryTrafficUnitsContainingAndQuery(@Param("unit_id") Long searchQueryTrafficUnitId, @Param("query") String query, Pageable pageable);

    @Query("select view from CustomErdiView view " +
            "where concat(view.id, '') like lower(concat('%',:query,'%')) " +
            "or lower(view.name) like lower(concat('%',:query,'%')) " +
            "or lower(view.unitType) like lower(concat('%',:query,'%')) " +
            "or lower(view.unitValue) like lower(concat('%',:query,'%')) " +
            "or lower(view.subtypeId) like lower(concat('%',:query,'%')) "
    )
    Page<CustomErdiView> findAllByQuery(@Param("query") String query, Pageable pageable);
}
