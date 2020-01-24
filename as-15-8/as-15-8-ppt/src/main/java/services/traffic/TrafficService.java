package services.traffic;

import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import liquibase.util.StringUtils;
import lombok.RequiredArgsConstructor;
import model.catalog.AccessToolsCategory;
import model.enums.AccessToolType;
import model.enums.TrafficType;
import model.enums.TrafficUnitType;
import model.traffic.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.AccessToolsCategoriesRepo;
import repositories.TrafficRepository;
import utils.TrafficUnitUtils;
import webClients.PodWebClient;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TrafficService {

    private final TrafficRepository trafficRepository;
    private final AccessToolsCategoriesRepo accessToolsCategoriesRepo;
    private final PodWebClient podWebClient;

    public Page<TrafficBriefView> getBriefTrafficList(SortingDirection sortingDirection,
                                                      String sortingColumn,
                                                      int pageNumber, int pageSize,
                                                      String query,
                                                      AccessToolType accessToolType,
                                                      String filteredName) {

        if (filteredName != null && !filteredName.isEmpty())
            query = filteredName;

        PageRequest pageable = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));

        Page<Traffic> traffics = trafficRepository.findAll(createTrafficSpecification(query, accessToolType), pageable);
        List<TrafficBriefView> views = traffics.getContent().parallelStream().map(traffic ->
                createTrafficBriefView(traffic)).collect(Collectors.toList());

        return new PageImpl<>(views, pageable, traffics.getTotalElements());
    }

    private TrafficBriefView createTrafficBriefView(Traffic traffic) {
        TrafficBriefView view = new TrafficBriefView(traffic.getId(), traffic.getName());
        long formalErdiCount = traffic.getActualCheckUnitsCount();
        long customErdiCount = trafficRepository.countCustomErdiByTrafficId(traffic.getId());
        long dynamicCount = 0;
        long staticCount = formalErdiCount + customErdiCount;
        view.setCount(staticCount + dynamicCount);
        view.setType(getTrafficType(staticCount, dynamicCount));
        return view;
    }

    public Long actualizeTrafficCheckUnitsCount(Traffic traffic) {
        Long actualCheckUnitsCount = getActualTrafficCheckUnitCount(traffic);
        traffic.setActualCheckUnitsCount(actualCheckUnitsCount);
        trafficRepository.save(traffic);
        return actualCheckUnitsCount;
    }

    private Long getActualTrafficCheckUnitCount(Traffic traffic) {
        try {
            List<Long> erdiIds = trafficRepository.allContentErdiByTrafficId(traffic.getId());
            Long actualCheckUnitsCount = podWebClient.calculateActualCheckUnitCount(traffic, erdiIds);
            return actualCheckUnitsCount;
        } catch (Exception e) {
            return 0L;
        }
    }

    private Map<Traffic, List<Long>> createTrafficErdiIdsKV(Page<Traffic> traffics) {
        Map<Traffic, List<Long>> mapTraffics =
                traffics.getContent()
                        .stream()
                        .collect(Collectors.toMap(
                                traffic -> traffic,
                                traffic -> trafficRepository.allContentErdiByTrafficId(traffic.getId())
                        ));
        return mapTraffics;
    }

    private TrafficType getTrafficType(long staticCount, long dynamicCount) {
        if (staticCount == 0 && dynamicCount > 0)
            return TrafficType.DYNAMIC;
        else if (staticCount > 0 && dynamicCount == 0)
            return TrafficType.STATIC;
        else
            return TrafficType.MIXED;
    }

    public TrafficFullView getTrafficById(Long id) {
        return trafficRepository.findById(id)
                .map(this::convertToFullView)
                .orElseThrow(() -> new AS_15_8_PPT_Exception(
                        "Traffic was not found by id: " + id));
    }

    public TrafficFullView createTraffic() {
        AccessToolsCategory category = accessToolsCategoriesRepo
                .findOneByOrderByIdDesc();
        String trafficName = getAvailableTrafficName();

        Traffic traffic = new Traffic();
        traffic.setName(trafficName);
        traffic.getErdiTrafficUnits().add(createErdiTrafficUnit(
                traffic, TrafficUnitType.FORMAL, category));
        traffic.getErdiTrafficUnits().add(createErdiTrafficUnit(
                traffic, TrafficUnitType.CUSTOM, category));
        traffic.getSearchQueryTrafficUnits().add(createSearchTrafficUnit(
                traffic, TrafficUnitType.TEMPLATE, category));

        traffic.setActualCheckUnitsCount(getActualTrafficCheckUnitCount(traffic));
        return convertToFullView(trafficRepository.save(traffic));
    }

    @Transactional
    public TrafficFullView updateTraffic(TrafficFullView fullView, Traffic oldTraffic) {
        boolean changeName = fullView.getName().compareTo(oldTraffic.getName()) != 0;

        oldTraffic.setName(fullView.getName());

        if (changeName) {
            getUnitStream(oldTraffic)
                    .forEach(unit -> unit.setName(TrafficUnitUtils
                            .getUpdateName(oldTraffic, unit.getName())));
        }

        syncAssociation(oldTraffic);

        oldTraffic.setActualCheckUnitsCount(getActualTrafficCheckUnitCount(oldTraffic));
        return convertToFullView(trafficRepository.save(oldTraffic));
    }

    public void deleteTraffic(Long id) {
        trafficRepository.deleteById(id);
    }

    private TrafficFullView convertToFullView(Traffic traffic) {
        TrafficFullView fullView = new TrafficFullView();
        fullView.setId(traffic.getId());
        fullView.setName(traffic.getName());
        getUnitStream(traffic).forEach(fullView::setUnit);
        return fullView;
    }

    private void syncAssociation(Traffic traffic) {
        getUnitStream(traffic)
                .peek(unit -> unit.setTraffic(traffic))
                .forEach(TrafficUnit::syncContentAssociation);
    }

    private Stream<TrafficUnit> getUnitStream(Traffic traffic) {
        List<ErdiTrafficUnit> erdiUnits = traffic.getErdiTrafficUnits();
        List<SearchQueryTrafficUnit> searchUnits = traffic.getSearchQueryTrafficUnits();
        if (erdiUnits != null && searchUnits != null) {
            return Stream.concat(
                    erdiUnits.stream().map(TrafficUnit.class::cast),
                    searchUnits.stream().map(TrafficUnit.class::cast)
            );
        } else if (erdiUnits != null) {
            return erdiUnits.stream().map(TrafficUnit.class::cast);
        } else if (searchUnits != null) {
            return searchUnits.stream().map(TrafficUnit.class::cast);
        } else {
            return Stream.empty();
        }
    }

    // todo multithreading & db changes
    private String getAvailableTrafficName() {
        Function<Integer, String> fName = n -> "Traffic " + n;
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            String name = fName.apply(i);
            boolean exists = trafficRepository.existsByName(name);
            if ( !exists ) return name;
        }
        return TrafficUnitUtils.generateRandomStringBounded();
    }

    // todo unite following methods
    private ErdiTrafficUnit createErdiTrafficUnit(Traffic traffic,
                                                  TrafficUnitType type,
                                                  AccessToolsCategory category) {
        String name = TrafficUnitUtils.getNewName(traffic.getName(), type);
        ErdiTrafficUnit unit = new ErdiTrafficUnit();
        unit.setName(name);
        unit.setCategory(category);
        unit.setTraffic(traffic);
        return unit;
    }

    private SearchQueryTrafficUnit createSearchTrafficUnit(Traffic traffic,
                                                           TrafficUnitType type,
                                                           AccessToolsCategory category) {
        String name = TrafficUnitUtils.getNewName(traffic.getName(), type);
        SearchQueryTrafficUnit unit = new SearchQueryTrafficUnit();
        unit.setName(name);
        unit.setCategory(category);
        unit.setTraffic(traffic);
        return unit;
    }

    private Specification<Traffic> createTrafficSpecification(String query, AccessToolType type) {
        return (Specification<Traffic>) (root, criteriaQuery, criteriaBuilder) -> {
            Predicate predicate = StringUtils.isEmpty(query) ? null :
                    criteriaBuilder.like(criteriaBuilder.upper(root.get(Traffic_.name)), "%" + query.toUpperCase() + "%");

            if (type == AccessToolType.PASD) {
                Subquery<Long> unitSubQuery = criteriaQuery.subquery(Long.class);
                Root<SearchQueryTrafficUnit> unitRoot = unitSubQuery.from(SearchQueryTrafficUnit.class);

                Subquery<Long> phraseSubQuery = unitSubQuery.subquery(Long.class);
                Root<SearchQueryTrafficUnitPhrase> phraseRoot = phraseSubQuery.from(SearchQueryTrafficUnitPhrase.class);
                Path<Long> joinPhraseUnitId = phraseRoot.get(SearchQueryTrafficUnitPhrase_.trafficUnit).get(SearchQueryTrafficUnit_.id);
                phraseSubQuery.select(joinPhraseUnitId);
                phraseSubQuery.where(criteriaBuilder.equal(joinPhraseUnitId, unitRoot.get(SearchQueryTrafficUnit_.id)));

                unitSubQuery.select(unitRoot.get(SearchQueryTrafficUnit_.traffic).get(Traffic_.id));
                unitSubQuery.where(
                        criteriaBuilder.equal(unitRoot.get(SearchQueryTrafficUnit_.traffic).get(Traffic_.id), root.get(Traffic_.id)),
                        criteriaBuilder.or(
                                criteriaBuilder.like(unitRoot.get(SearchQueryTrafficUnit_.name), "%TEMPLATE"),
                                criteriaBuilder.exists(phraseSubQuery)
                        )
                );
                Predicate typePredicate = root.get(Traffic_.id).in(unitSubQuery);
                predicate = predicate == null ? typePredicate : criteriaBuilder.and(predicate, typePredicate);
            }
            return predicate;
        };
    }

}
