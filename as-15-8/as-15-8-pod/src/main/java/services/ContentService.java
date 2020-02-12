package services;

import checkUnits.CheckUnitType;
import lombok.RequiredArgsConstructor;
import model.actualViews.ContentCheckUnit;
import model.projection.ContentView;
import model.scheme.DomainMask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import repositories.ContentCheckUnitRepository;
import repositories.ContentViewRepository;
import repositories.DomainMaskRepo;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Service
@CacheConfig(cacheNames={"sor_content"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ContentService {

    private final ContentViewRepository viewRepository;
    private final ContentCheckUnitRepository contentCheckUnitRepository;
    private final DomainMaskRepo domainMaskRepo;

    @Cacheable
    public Optional<ContentView> getFormalErdiView(Long id) {
        return viewRepository.findById(id);
    }

    /**
     * Получение списка актуальных чек-юнитов по ИД ЕРДИ
     * @param erdiId ИД ЕРДИ
     * @return список актуальных чек-юнитов
     */
    public List<ContentCheckUnit> getActualCheckUnits(Long erdiId){
        return contentCheckUnitRepository.findAllByErdId(erdiId);
    }

    public List<ContentCheckUnit> getActualCheckUnits(List<Long> erdiIds){
        return contentCheckUnitRepository.findAllByErdIds(erdiIds);
    }

    public Long getCheckUnitsCount(List<Long> erdiIds) {
        Long erdiCountDomainMaskExclude = contentCheckUnitRepository.findAllByErdIdsExclude(erdiIds, CheckUnitType.DOMAIN_MASK);
        Long erdiDomainMaskCount = getCheckUnitsInDomainMaskRepo(erdiIds);
        return erdiCountDomainMaskExclude + erdiDomainMaskCount;
    }

    private Long getCheckUnitsInDomainMaskRepo(List<Long> erdiIds) {
        AtomicReference<Long> erdiDomainMaskCount = new AtomicReference<>(0L);
        List<String> erdiDomainMasksCheckUnitValues = contentCheckUnitRepository.findAllCheckUnitsValueBy(erdiIds, CheckUnitType.DOMAIN_MASK);
        erdiDomainMasksCheckUnitValues.forEach(checkUnitValue-> {
            Set<DomainMask> domainMasks = domainMaskRepo.findAllDomainMasksLike(checkUnitValue);
            domainMasks.forEach(domainMask -> {
                long domainsCount = domainMask.getDomains().size();
                erdiDomainMaskCount.set(erdiDomainMaskCount.get() + domainsCount);
            });
        });
        return erdiDomainMaskCount.get();
    }
}
