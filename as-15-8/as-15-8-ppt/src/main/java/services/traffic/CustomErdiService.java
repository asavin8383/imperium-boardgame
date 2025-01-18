package services.traffic;

import exceptions.AS_15_8_PPT_Exception;
import liquibase.util.StringUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.traffic.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import repositories.CustomErdiRepository;
import repositories.CustomErdiViewRepository;
import utils.MessageUtils;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CustomErdiService {

    private static final String TRAFFIC_ID_COLUMN = "id";

    private final CustomErdiRepository customErdiRepository;
    private final CustomErdiViewRepository viewRepository;
    private final TrafficService trafficService;

    private List<SingularAttribute<CustomErdiView, String>> searchQueryColumns;

    @PostConstruct
    public void init() {
        searchQueryColumns = new ArrayList<>(3);
        searchQueryColumns.add(CustomErdiView_.name);
        searchQueryColumns.add(CustomErdiView_.unitValue);
        searchQueryColumns.add(CustomErdiView_.unitType);
    }

    public Page<CustomErdiView> getCustomErdiView(Pageable pageable, String query,
                                                  boolean containsInTraffic,
                                                  Long erdiTrafficUnitId,
                                                  Long searchTrafficUnitId) {

        Specification<CustomErdiView> specification = getSpecification(query,
                containsInTraffic, erdiTrafficUnitId, searchTrafficUnitId);
        return specification == null ? viewRepository.findAll(pageable) :
                viewRepository.findAll(specification, pageable);
    }

    private Specification<CustomErdiView> getSpecification(String query,
                                                           boolean containsInTraffic,
                                                           Long erdiTrafficUnitId,
                                                           Long searchTrafficUnitId) {
        if (erdiTrafficUnitId != null)
            return containsInTrafficUnit(query, containsInTraffic, erdiTrafficUnitId,
                    CustomErdiView_.erdiTrafficUnits);
        else if (searchTrafficUnitId != null)
            return containsInTrafficUnit(query, containsInTraffic, searchTrafficUnitId,
                    CustomErdiView_.searchQueryPatterns);
        else if (query != null && query.trim().length() > 0)
            return (root, criteriaQuery, criteriaBuilder) ->
                    predicateContainsQuery(criteriaBuilder, root, query);

        return null;
    }

    private <T> Specification<CustomErdiView> containsInTrafficUnit(String query, boolean containsInTraffic,
                                                                    @NonNull Long trafficUnitId,
                                                                    ListAttribute<CustomErdiView, T> joinColumn) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Predicate predicate;

            if (containsInTraffic) {
                Join<CustomErdiView, T> join = root.join(joinColumn, JoinType.LEFT);
                predicate = criteriaBuilder.equal(join.get(TRAFFIC_ID_COLUMN), trafficUnitId);
            } else {
                Subquery<ErdiTrafficUnitCustom> sub = criteriaQuery.subquery(ErdiTrafficUnitCustom.class);
                Root<ErdiTrafficUnitCustom> subRoot = sub.from(ErdiTrafficUnitCustom.class);
                sub.select(subRoot);
                sub.where(criteriaBuilder.and(
                        criteriaBuilder.equal(subRoot.get(ErdiTrafficUnitCustom_.trafficUnit).get(ErdiTrafficUnit_.id), trafficUnitId),
                        criteriaBuilder.equal(subRoot.get(ErdiTrafficUnitCustom_.customErdi).get(CustomErdi_.id), root.get("id"))));
                predicate = criteriaBuilder.not(criteriaBuilder.exists(sub));
            }

            return StringUtils.isEmpty(query) ? predicate : criteriaBuilder.and(
                    predicate, predicateContainsQuery(criteriaBuilder, root, query));
        };
    }

    private Predicate predicateContainsQuery(CriteriaBuilder cb, Root<CustomErdiView> root, String query) {
        String likeQuery = "%" + query + "%";

        Predicate[] likePredicates = searchQueryColumns.stream()
                .map(column -> cb.like(cb.lower(root.get(column)), likeQuery.toLowerCase()))
                .toArray(Predicate[]::new);

        return cb.or(likePredicates);
    }

    public CustomErdi createCustomErdi(CustomErdi customErdi) {
        customErdi.getCustomErdiUnits().forEach(
                unit -> unit.setCustomErdi(customErdi));

        CustomErdi erdi = null;
        try{
            erdi = customErdiRepository.save(customErdi);
        }
        catch(Throwable e){
            MessageUtils.wrapAndThrowUserMessageException(e);
        }
        trafficService.actualizeCheckUnitsCount(erdi);
        return erdi;
    }

    public CustomErdi getCustomErdiById(Long id) {
        return customErdiRepository.findById(id).orElseThrow(() ->
                new AS_15_8_PPT_Exception("Custom ERDI was not found by id: " + id));
    }

    public CustomErdi updateCustomErdi(CustomErdi newCustomErdi,
                                       CustomErdi customErdi) {
        customErdi.setName(newCustomErdi.getName());
        customErdi.setSubtypeId(newCustomErdi.getSubtypeId());
        customErdi.getCustomErdiUnits().clear();
        newCustomErdi.getCustomErdiUnits().forEach(customErdiUnit -> customErdiUnit.setCustomErdi(customErdi));
        customErdi.getCustomErdiUnits().addAll(newCustomErdi.getCustomErdiUnits());
        CustomErdi erdi = customErdiRepository.save(customErdi);
        trafficService.actualizeCheckUnitsCount(erdi);
        return erdi;
    }

    public void deleteCustomErdi(Long id) {
        customErdiRepository.deleteById(id);
        customErdiRepository.findById(id).ifPresent(trafficService::actualizeCheckUnitsCount);
    }

}
