package services.traffic;

import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_Exception;
import lombok.RequiredArgsConstructor;
import model.enums.TrafficType;
import model.enums.TrafficUnitType;
import model.traffic.ErdiTrafficUnit;
import model.traffic.SearchQueryTrafficUnit;
import model.traffic.Traffic;
import model.traffic.TrafficUnit;
import model.traffic.projection.TrafficProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import repositories.TrafficRepository;
import utils.TrafficUnitUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.contains;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TrafficService {

    private final TrafficRepository trafficRepository;

    public Page<Traffic> getAllTraffic(SortingDirection sortingDirection,
                                       String sortingColumn,
                                       int pageNumber,
                                       int pageSize,
                                       String query) {

        PageRequest page = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));

        Traffic traffic = new Traffic();
        traffic.setName(query);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withMatcher("name", contains().ignoreCase());

        return trafficRepository.findAll(Example.of(traffic, matcher), page);
    }

    public Page<TrafficProjection> getAllTrafficInfo(SortingDirection sortingDirection,
                                                     String sortingColumn,
                                                     int pageNumber,
                                                     int pageSize,
                                                     String query) {

        PageRequest pageable = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        Page<TrafficProjection> page =
                trafficRepository.findAllTrafficInfo(query, pageable);
        page.getContent().parallelStream().forEach(traffic -> {
            long formalErdiCount = trafficRepository.countContentErdiByTrafficId(traffic.getId());
            long customErdiCount = trafficRepository.countCustomErdiByTrafficId(traffic.getId());
            long searchPhrasesCount = trafficRepository.countSearcPhrasesByTrafficId(traffic.getId());
            long searchTemplatesCount = trafficRepository.countSearchTemplatesByTrafficId(traffic.getId());
            long dynamicCount = trafficRepository.countDynamicByTrafficId(traffic.getId());
            long staticCount = formalErdiCount + customErdiCount + searchPhrasesCount + searchTemplatesCount;

            traffic.setCount(staticCount + dynamicCount);
            traffic.setType(getTrafficType(staticCount, dynamicCount));
        });
        return page;
    }

    public TrafficType getTrafficType(long staticCount, long dynamicCount) {
        if (staticCount == 0 && dynamicCount > 0)
            return TrafficType.DYNAMIC;
        else if (staticCount > 0 && dynamicCount == 0)
            return TrafficType.STATIC;
        else
            return TrafficType.MIXED;
    }

    public Traffic getTrafficById(Long id) {
        return trafficRepository.findById(id)
                .map(TrafficService::setBlockFields)
                .orElseThrow(() -> new AS_15_8_Exception(
                        "Traffic was not found by id: " + id));
    }

    public Traffic createTraffic(Traffic traffic) {
        traffic.setTrafficUnits(getNonEmptyUnits(traffic));
        traffic.getTrafficUnits().forEach(unit -> {
                    unit.setTraffic(traffic);
                    unit.setName(TrafficUnitUtils.getNewName(traffic,
                            Objects.requireNonNull(unit.getType())));
                });

        return setBlockFields(trafficRepository.save(traffic));
    }

    public Traffic updateTraffic(Traffic newTraffic, Traffic oldTraffic) {
        boolean updateUnitNames =
                newTraffic.getName().compareTo(oldTraffic.getName()) != 0;

        newTraffic.setTrafficUnits(getNonEmptyUnits(newTraffic));
        oldTraffic.setName(newTraffic.getName());
        oldTraffic.setTrafficUnits(newTraffic.getTrafficUnits());

        if (updateUnitNames) {
            oldTraffic.getTrafficUnits().stream()
                    .filter(unit -> Objects.nonNull(unit.getName()))
                    .forEach(unit -> unit.setName(TrafficUnitUtils
                            .getUpdateName(oldTraffic, unit.getName())));
        }

        oldTraffic.getTrafficUnits().forEach(unit -> unit.setTraffic(oldTraffic));
        oldTraffic.getTrafficUnits().stream()
                .filter(unit -> Objects.isNull(unit.getName()))
                .forEach(unit -> unit.setName(TrafficUnitUtils
                        .getNewName(oldTraffic, Objects.requireNonNull(unit.getType()))));

        return setBlockFields(trafficRepository.save(oldTraffic));
    }

    public void deleteTraffic(Long id) {
        trafficRepository.deleteById(id);
    }

    private List<TrafficUnit> getNonEmptyUnits(Traffic traffic) {
        List<TrafficUnit> units = collectTrafficUnits(traffic);
        return units.stream()
                .filter(Objects::nonNull)
                .filter(TrafficUnit::nonEmpty)
                .collect(Collectors.toList());
    }

    public static List<TrafficUnit> collectTrafficUnits(Traffic traffic) {
        List<TrafficUnit> units = new LinkedList<>();

        ErdiTrafficUnit formalErdiUnit = traffic.getFormalErdiUnit();
        formalErdiUnit.getFormalErdiList().forEach(erdiContentJoin ->
                erdiContentJoin.setTrafficUnit(formalErdiUnit));
        units.add(formalErdiUnit);

        units.add(traffic.getCustomErdiUnit());
        units.add(traffic.getSearchPhraseUnit());

        List<SearchQueryTrafficUnit> templates = traffic.getSearchTemplates();
        if (! CollectionUtils.isEmpty(templates) ) {
            templates.forEach(template -> template.getFormalErdiList().forEach(
                    searchQueryContentJoin -> searchQueryContentJoin.setTrafficUnit(template)));
            units.addAll(templates);
        }
        return units;
    }

    public static Traffic setBlockFields(Traffic traffic) {
        List<SearchQueryTrafficUnit> searchTemplates = new LinkedList<>();
        for (TrafficUnit trafficUnit : traffic.getTrafficUnits()) {
            TrafficUnitType type = TrafficUnitUtils.getType(trafficUnit);
            switch (type) {
                case FORMAL:
                    traffic.setFormalErdiUnit((ErdiTrafficUnit) trafficUnit);
                    break;
                case CUSTOM:
                    traffic.setCustomErdiUnit((ErdiTrafficUnit) trafficUnit);
                    break;
                case PHRASE:
                    traffic.setSearchPhraseUnit((SearchQueryTrafficUnit) trafficUnit);
                    break;
                case TEMPLATE:
                    searchTemplates.add((SearchQueryTrafficUnit) trafficUnit);
                    break;
                case DYNAMIC:
                    throw new UnsupportedOperationException("DYNAMIC not implemented");
            }
        }
        if (! CollectionUtils.isEmpty(searchTemplates) )
            traffic.setSearchTemplates(searchTemplates);
        return traffic;
    }

}
