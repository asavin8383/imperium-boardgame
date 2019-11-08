package services;

import lombok.RequiredArgsConstructor;
import model.actualViews.ContentCheckUnit;
import model.projection.ContentView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import repositories.ContentCheckUnitRepository;
import repositories.ContentViewRepository;
import utils.ContentViewSpecifications;

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
        Specification<ContentView> specification = StringUtils.isEmpty(query) ?
                null : ContentViewSpecifications.containsQueryString(query);
        return specification == null ? viewRepository.findAll(pageable) :
                viewRepository.findAll(specification, pageable);
    }

    @Cacheable
    public Page<ContentView> getFormalErdiView(List<Long> ids, Pageable pageable) {
        return viewRepository.findAllByIdIn(ids, pageable);
    }

    @Cacheable
    public Optional<ContentView> getFormalErdiView(Long id) {
        return viewRepository.findById(id);
    }

    /**
     * Получение списка актуальных чек-юнитов по ИД ЕРДИ
     * @param contentId ИД ЕРДИ
     * @return список актуальных чек-юнитов
     */
    public List<ContentCheckUnit> getActualCheckUnits(Long contentId){
        return contentCheckUnitRepository.findAllByContentId(contentId);
    }

}
