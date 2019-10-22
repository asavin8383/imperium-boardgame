package repositories;

import model.enums.Dictionary;
import model.scheme.PasdRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import repositories.helper.DictionaryRepository;

import java.util.Date;

@Repository
public interface PasdRepository extends
        JpaRepository<PasdRecord, Integer>,
        PasdRepositoryCustom,
        DictionaryRepository {

    long countByEffDt(Date effDt);

    // to do js - get single column
    PasdRecord findFirstByEffDtOrderByCrDateDesc(Date effDt);

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
        return findFirstByEffDtOrderByCrDateDesc(effDt).getCrDate();
    }
}
