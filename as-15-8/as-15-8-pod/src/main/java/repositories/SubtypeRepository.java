package repositories;

import enums.Dictionary;
import model.scheme.Subtype;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import repositories.helper.DictionaryRepository;
import utils.Utils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Repository
public interface SubtypeRepository extends JpaRepository<Subtype, Integer>, DictionaryRepository {

    long countByEffDt(Date effDt);

    Subtype findTopByOrigIdOrderByIdDesc(String origId);

    @Query(value = "select max(c_date) from sor.ps where eff_dt = :effDt", nativeQuery = true)
    Date findMaxCDateByEffDt(@Param("effDt") Date effDt);

    @Override
    default Dictionary getDictionaryType() {
        return Dictionary.SUBTYPE;
    }

    @Override
    default long getCountByEffDt(Date effDt) {
        return this.countByEffDt(effDt);
    }

    @Override
    default Date getUpdateDateTime(Date effDt) {
        return findMaxCDateByEffDt(effDt);
    }

    Optional<Subtype> findByOrigIdAndEffDt(String origId, Date effDt);

    @Query("select distinct s from Subtype s " +
            "where s.effDt = :effDt " +
            "or concat(s.id, '') like lower(concat('%',:query,'%')) " +
            "or lower(s.origId) like lower(concat('%',:query,'%')) " +
            "or lower(s.registryName) like lower(concat('%',:query,'%')) " +
            "or lower(s.categoryName) like lower(concat('%',:query,'%'))" +
            "or lower(s.violationName) like lower(concat('%',:query,'%'))"
        )
    Page<Subtype> findByEffDtAndQuery(Date effDt, String query, Pageable pageable);
}
