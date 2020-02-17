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
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TrafficService {

    private final TrafficRepository trafficRepository;
    private final AccessToolsCategoriesRepo accessToolsCategoriesRepo;
    private final PodWebClient podWebClient;
    private static long dynamicCount = 0;

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

        view.setActualCheckUnitsCount(calculateActualCheckUnitsCount(traffic));
        view.setErdiCount(calculateErdiCount(traffic));

        view.setType(TrafficType.MIXED);

        return view;
    }

    private Long calculateActualCheckUnitsCount(Traffic traffic) {
        long formalErdiCount = traffic.getActualCheckUnitsCount();
        long customErdiCount = trafficRepository.countCustomErdiByTrafficId(traffic.getId());

        long staticCount = formalErdiCount + customErdiCount;
        return staticCount + dynamicCount;
    }

    private Long calculateErdiCount(Traffic traffic) {
        long formalErdiCount = trafficRepository.countContentErdiByTrafficId(traffic.getId());
        long customErdiCount = trafficRepository.countCustomErdiByTrafficId(traffic.getId());
        long staticCount = formalErdiCount + customErdiCount;
        return staticCount;
    }

    public Long actualizeTrafficCheckUnitsCount(Long trafficId) {
        Optional<Traffic> traffic = trafficRepository.findById(trafficId);
        traffic.orElseThrow(() ->
                new AS_15_8_PPT_Exception("Ошибка при актуализации количества чек юнитов для трафика, трафик с такми id не найден: "+ trafficId));

        Long actualCheckUnitsCount = getActualTrafficCheckUnitCount(trafficId);
        traffic.get().setActualCheckUnitsCount(actualCheckUnitsCount);
        trafficRepository.save(traffic.get());
        return actualCheckUnitsCount;
    }

    private Long getActualTrafficCheckUnitCount(Long trafficId) {
        try {
            List<Long> erdiIds = trafficRepository.allContentErdiByTrafficId(trafficId);
            Long actualCheckUnitsCount = podWebClient.calculateActualCheckUnitCount(erdiIds);
            return actualCheckUnitsCount;
        } catch (Exception e) {
            return 0L;
        }
    }

    public void actualizeCheckUnitsCountForAllTraffic() {
        List<Traffic> traffics = trafficRepository.findAll();

        Map<Traffic, List<Long>> mapTraffics = createTrafficErdiIdsKV(traffics);

        List<TrafficBriefView> views = Flux.fromIterable(mapTraffics.entrySet())
                .parallel(50)
                .runOn(Schedulers.parallel())
                .flatMap(trafficEntry -> podWebClient.fetchActualCheckUnitCount(trafficEntry.getKey(), trafficEntry.getValue()))
                .sequential()
                .collectList()
                .block();
    }

    private Map<Traffic, List<Long>> createTrafficErdiIdsKV(List<Traffic> traffics) {
        Map<Traffic, List<Long>> mapTraffics = traffics.stream()
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



        traffic.getErdiTrafficUnits().add((ErdiTrafficUnit) fillTrafficUnit(new ErdiTrafficUnit(),
                                                                                traffic,
                                                                                TrafficUnitType.FORMAL,
                                                                                category));

        traffic.getErdiTrafficUnits().add((ErdiTrafficUnit) fillTrafficUnit(new ErdiTrafficUnit(),
                                                                                traffic,
                                                                                TrafficUnitType.CUSTOM,
                                                                                category));

        traffic.getErdiTrafficUnits().add((ErdiTrafficUnit) fillTrafficUnit(new ErdiTrafficUnit(),
                                                                                traffic,
                                                                                TrafficUnitType.TEMPLATE,
                                                                                category));

        traffic.getErdiTrafficUnits().add((ErdiTrafficUnit) fillTrafficUnit(new ErdiTrafficUnit(),
                                                                                traffic,
                                                                                TrafficUnitType.DYNAMIC,
                                                                                category));

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

    private TrafficUnit fillTrafficUnit(TrafficUnit trafficUnit,
                                                        Traffic traffic,
                                                        TrafficUnitType type,
                                                        AccessToolsCategory category) {
        String name = TrafficUnitUtils.getNewName(traffic.getName(), type);

        trafficUnit.setName(name);
        trafficUnit.setCategory(category);
        trafficUnit.setTraffic(traffic);
        return trafficUnit;
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
