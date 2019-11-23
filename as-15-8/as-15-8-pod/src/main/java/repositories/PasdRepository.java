package repositories;

import enums.Dictionary;
import model.scheme.PasdRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import repositories.helper.DictionaryRepository;

import java.util.Date;
import java.util.List;

@Repository
public interface PasdRepository extends
        JpaRepository<PasdRecord, Integer>,
        PasdRepositoryCustom,
        DictionaryRepository {

    long countByEffDt(Date effDt);

    @Query(value = "select pasd from PasdRecord pasd where eff_dt = '3000-01-01'")
    List<PasdRecord> getAllActial();

    @Query(value = "select max(c_date) from sor.pasd where eff_dt = :effDt", nativeQuery = true)
    Date findMaxCDateByEffDt(@Param("effDt") Date effDt);

    @Override
    default Dictionary getDictionaryType() {
        return Dictionary.PASD;
    }

    @Override
    default long getCountByEffDt(Date effDt) {
        return this.countByEffDt(effDt);
    }

    @Override
    default Date getUpdateDateTime(Date effDt) {
        return findMaxCDateByEffDt(effDt);
    }
}
