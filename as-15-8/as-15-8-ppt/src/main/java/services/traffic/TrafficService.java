package services.traffic;

import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import model.enums.TrafficType;
import model.traffic.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import repositories.TrafficRepository;
import utils.TrafficUnitUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TrafficService {

    private final EntityManager em;
    private final TrafficRepository trafficRepository;

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
                .map(TrafficService::convertToFullView)
                .orElseThrow(() -> new AS_15_8_PPT_Exception(
                        "Traffic was not found by id: " + id));
    }

    public TrafficFullView createTraffic(TrafficFullView fullView) {
        Traffic traffic = convertToTraffic(fullView);
        setNewUnitNames(traffic);
        return convertToFullView(trafficRepository.save(traffic));
    }

    public TrafficFullView updateTraffic(TrafficFullView fullView, Traffic oldTraffic) {
        boolean updateNames = fullView.getName().compareTo(oldTraffic.getName()) != 0;

        Traffic newTraffic = convertToTraffic(fullView);
        newTraffic.setId(oldTraffic.getId());

        if (updateNames) {
            setUnitNames(newTraffic.getErdiTrafficUnits(),
                    newTraffic.getSearchQueryTrafficUnits(),
                    unit -> {
                if (StringUtils.isEmpty(unit.getName()))
                    unit.setName(TrafficUnitUtils
                            .getNewName(newTraffic, unit.getType()));
                else
                    unit.setName(TrafficUnitUtils
                            .getUpdateName(newTraffic, unit.getName()));
            });
        } else {
            setNewUnitNames(newTraffic);
        }

        return convertToFullView(trafficRepository.save(newTraffic));
    }

    public void deleteTraffic(Long id) {
        trafficRepository.deleteById(id);
    }

    private Traffic convertToTraffic(TrafficFullView fullView) {
        Traffic traffic = new Traffic();
        traffic.setId(fullView.getId());
        traffic.setName(fullView.getName());
        traffic.setSearchQueryTrafficUnits(collectUnits(
                Stream.of(fullView.getSearchPhraseUnit()),
                fullView.getSearchTemplates().stream(),
                traffic));
        traffic.setErdiTrafficUnits(collectUnits(
                Stream.of(fullView.getFormalErdiUnit()),
                Stream.of(fullView.getCustomErdiUnit()),
                traffic));
        return traffic;
    }

    private static TrafficFullView convertToFullView(Traffic traffic) {
        TrafficFullView fullView = new TrafficFullView();
        fullView.setId(traffic.getId());
        fullView.setName(traffic.getName());
        Stream.concat(traffic.getErdiTrafficUnits().stream(),
                traffic.getSearchQueryTrafficUnits().stream())
                .forEach(fullView::setUnit);
        return fullView;
    }

    private <T extends TrafficUnit> List<T> collectUnits(Stream<T> a, Stream<T> b,
                                                         Traffic traffic) {
        return Stream.concat(a, b)
                .filter(Objects::nonNull)
                .filter(TrafficUnit::nonEmpty)
                .peek(unit -> unit.setTraffic(traffic))
                .peek(TrafficUnit::syncContentAssociation)
                .collect(Collectors.toList());
    }

    private void setNewUnitNames(Traffic traffic) {
        setUnitNames(traffic.getErdiTrafficUnits(),
                traffic.getSearchQueryTrafficUnits(),
                unit -> unit.setName(TrafficUnitUtils
                        .getNewName(traffic, unit.getType())));
    }

    private void setUnitNames(List<ErdiTrafficUnit> erdiTrafficUnits,
                                     List<SearchQueryTrafficUnit> searchQueryTrafficUnits,
                                     Consumer<TrafficUnit> updater) {
        Stream.concat(
                erdiTrafficUnits.stream().map(TrafficUnit.class::cast),
                searchQueryTrafficUnits.stream().map(TrafficUnit.class::cast)
        ).forEach(updater);
    }

}
