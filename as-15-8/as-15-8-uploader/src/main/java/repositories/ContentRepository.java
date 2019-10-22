package repositories;

import model.enums.Dictionary;
import model.scheme.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import repositories.helper.DictionaryRepository;
import utils.Utils;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;


@Repository
public interface ContentRepository extends
        JpaRepository<Content, Long>,
        ContentRepositoryCustom,
        DictionaryRepository {

    List<Content> findByErdiIdIn(List<String> ids);

    List<Content> findByIdIn(List<Long> ids);

    @Transactional
    void deleteByContentVersionId(Long contentVersionId);

        @Override
    default Dictionary getDictionaryType() {
        return Dictionary.ERDI;
    }

    @Override
    default long getCountByEffDt(Date effDt) {
        return this.findRelevantCount();
    }

    @Override
    default Date getUpdateDateTime(Date effDt) {
        return Utils.getEndDate();
    }

}
