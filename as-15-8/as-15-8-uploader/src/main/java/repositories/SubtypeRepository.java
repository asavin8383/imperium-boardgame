package repositories;

import model.enums.Dictionary;
import model.scheme.Subtype;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import repositories.helper.DictionaryRepository;

import java.time.LocalDateTime;

@Repository
public interface SubtypeRepository extends
        JpaRepository<Subtype, Integer>,
        SubtypeRepositoryCustom,
        DictionaryRepository {

    long countByEffDt(LocalDateTime effDt);

    // to do js - get single column
    Subtype findFirstByEffDtOrderByCrDateDesc(LocalDateTime effDt);

    @Override
    default Dictionary getDictionaryType() {
        return Dictionary.SUBTYPE;
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
