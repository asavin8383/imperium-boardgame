package repositories;

import model.traffic.SearchPhrase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import repositories.helpers.SearchPhraseParams;

public interface SearchPhraseRepositoryCustom {

    Page<SearchPhrase> searchFor(Class<SearchPhrase> clazz, SearchPhraseParams params, Pageable pageable);

}
