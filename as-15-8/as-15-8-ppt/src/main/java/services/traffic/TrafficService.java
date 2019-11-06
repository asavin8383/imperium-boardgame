package services.traffic;

import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import model.catalog.AccessToolsCategory;
import model.enums.TrafficType;
import model.enums.TrafficUnitType;
import model.traffic.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.AccessToolsCategoriesRepo;
import repositories.TrafficRepository;
import utils.TrafficUnitUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TrafficService {

    private final TrafficRepository trafficRepository;
    private final AccessToolsCategoriesRepo accessToolsCategoriesRepo;

    public Page<TrafficBriefView> getBriefTrafficList(SortingDirection sortingDirection,
                                                      String sortingColumn,
                                                      int pageNumber,
                                                      int pageSize,
                                                      String query) {

        PageRequest pageable = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        Page<TrafficBriefView> page =
                trafficRepository.findAllTrafficInfo(query, pageable);
        page.getContent().parallelStream().forEach(traffic -> {
            long formalErdiCount = trafficRepository.countContentErdiByTrafficId(traffic.getId());
            long customErdiCount = trafficRepository.countCustomErdiByTrafficId(traffic.getId());
            long searchPhrasesCount = trafficRepository.countSearchPhrasesByTrafficId(traffic.getId());
            long searchTemplatesCount = trafficRepository.countSearchTemplatesByTrafficId(traffic.getId());
            long dynamicCount = 0; //trafficRepository.countDynamicByTrafficId(traffic.getId());
            long staticCount = formalErdiCount + customErdiCount + searchPhrasesCount + searchTemplatesCount;

            traffic.setCount(staticCount + dynamicCount);
            traffic.setType(getTrafficType(staticCount, dynamicCount));
        });
        return page;
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
        traffic.setErdiTrafficUnits(new ArrayList<>(2));
        traffic.getErdiTrafficUnits().add(createErdiTrafficUnit(
                traffic, TrafficUnitType.FORMAL, category));
        traffic.getErdiTrafficUnits().add(createErdiTrafficUnit(
                traffic, TrafficUnitType.CUSTOM, category));
        traffic.setSearchQueryTrafficUnits(new ArrayList<>(1));
        traffic.getSearchQueryTrafficUnits().add(createSearchTrafficUnit(
                traffic, TrafficUnitType.PHRASE, category));
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

}
