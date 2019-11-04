package services;

import lombok.RequiredArgsConstructor;
import model.actualViews.ContentCheckUnit;
import model.portal.ErdiTrafficUnitJoin;
import model.portal.SearchQueryTrafficUnitJoin;
import model.projection.ContentView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import repositories.ContentCheckUnitRepository;
import repositories.ContentViewRepository;
import utils.ContentViewSpecifications;

import java.util.List;

@Service
@CacheConfig(cacheNames={"sor_content"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ContentService {

    public static final String ERDI_TRAFFIC_UNIT_COLUMN = "erdiTrafficUnits";
    public static final String SEARCH_QUERY_TRAFFIC_UNIT_COLUMN = "searchQueryTrafficUnits";

    private final ContentViewRepository viewRepository;
    private final ContentCheckUnitRepository contentCheckUnitRepository;

    @Cacheable
    public Page<ContentView> getFormalErdiView(Pageable pageable,
                                               String query,
                                               boolean containsInTraffic,
                                               Long erdiTrafficUnitId,
                                               Long searchTrafficUnitId) {
        Specification<ContentView> specification = getSpecification(query, containsInTraffic,
                erdiTrafficUnitId, searchTrafficUnitId);
        return specification == null ? viewRepository.findAll(pageable) :
                viewRepository.findAll(specification, pageable);
    }

    /**
     * Получение списка актуальных чек-юнитов по ИД ЕРДИ
     * @param contentId ИД ЕРДИ
     * @return список актуальных чек-юнитов
     */
    public List<ContentCheckUnit> getActualCheckUnits(Long contentId){
        return contentCheckUnitRepository.findAllByContentId(contentId);
    }

    private Specification<ContentView> getSpecification(String query,
                                                        boolean containsInTraffic,
                                                        Long erdiTrafficUnitId,
                                                        Long searchTrafficUnitId) {
        if (erdiTrafficUnitId != null)
            return containsInTraffic ?
                    ContentViewSpecifications.<ErdiTrafficUnitJoin>containsInTrafficUnit(
                            query, ERDI_TRAFFIC_UNIT_COLUMN, erdiTrafficUnitId) :
                    ContentViewSpecifications.notContainsInTrafficUnit(
                            query, erdiTrafficUnitId, ErdiTrafficUnitJoin.class);
        else if (searchTrafficUnitId != null)
            return containsInTraffic ?
                    ContentViewSpecifications.<SearchQueryTrafficUnitJoin>containsInTrafficUnit(
                            query, SEARCH_QUERY_TRAFFIC_UNIT_COLUMN, searchTrafficUnitId) :
                    ContentViewSpecifications.notContainsInTrafficUnit(
                            query, searchTrafficUnitId, SearchQueryTrafficUnitJoin.class);
        else if (query != null && query.trim().length() > 0)
            return ContentViewSpecifications.containsQueryString(query);

        return null;
    }

}
