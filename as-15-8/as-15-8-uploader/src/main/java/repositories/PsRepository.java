package repositories;

import model.enums.Dictionary;
import model.scheme.PsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import repositories.helper.DictionaryRepository;

import java.time.LocalDateTime;

@Repository
public interface PsRepository extends
        JpaRepository<PsRecord, Integer>,
        PsRepositoryCustom,
        DictionaryRepository {

    long countByEffDt(LocalDateTime effDt);

    // to do js - get single column
    PsRecord findFirstByEffDtOrderByCrDateDesc(LocalDateTime effDt);

    @Override
    default Dictionary getDictionaryType() {
        return Dictionary.PS;
    }

    @Override
    default long getCountByEffDt(LocalDateTime effDt) {
        return this.countByEffDt(effDt);
    }

    @Override
    default LocalDateTime getUpdateDateTime(LocalDateTime effDt) {
        return findFirstByEffDtOrderByCrDateDesc(effDt).getCrDate();
    }

}
