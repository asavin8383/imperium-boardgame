package repositories;

import model.enums.Dictionary;
import model.scheme.PsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import repositories.helper.DictionaryRepository;

import java.util.Date;

@Repository
public interface PsRepository extends
        JpaRepository<PsRecord, Integer>,
        PsRepositoryCustom,
        DictionaryRepository {

    long countByEffDt(Date effDt);

    // to do js - get single column
    PsRecord findFirstByEffDtOrderByCrDateDesc(Date effDt);

    @Override
    default Dictionary getDictionaryType() {
        return Dictionary.PS;
    }

    @Override
    default long getCountByEffDt(Date effDt) {
        return this.countByEffDt(effDt);
    }

    @Override
    default Date getUpdateDateTime(Date effDt) {
        return findFirstByEffDtOrderByCrDateDesc(effDt).getCrDate();
    }

}
