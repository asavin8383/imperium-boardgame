package services;

import lombok.RequiredArgsConstructor;
import model.actualViews.ContentCheckUnit;
import model.projection.ContentView;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import repositories.ContentCheckUnitRepository;
import repositories.ContentViewRepository;

import java.util.List;
import java.util.Optional;

@Service
@CacheConfig(cacheNames={"sor_content"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ContentService {

    private final ContentViewRepository viewRepository;
    private final ContentCheckUnitRepository contentCheckUnitRepository;

    @Cacheable
    public Page<ContentView> getFormalErdiView(String query, Pageable pageable) {
        if(Strings.isNotEmpty(query))
            return viewRepository.findAllByQuery(query, pageable);
        else
            return viewRepository.findAll(pageable);
    }

    @Cacheable
    public Optional<ContentView> getFormalErdiView(String id) {
        return viewRepository.findById(id);
    }

    /**
     * Получение списка актуальных чек-юнитов по ИД ЕРДИ
     * @param erdiId ИД ЕРДИ
     * @return список актуальных чек-юнитов
     */
    public List<ContentCheckUnit> getActualCheckUnits(String erdiId){
        return contentCheckUnitRepository.findAllByErdId(erdiId);
    }

}
