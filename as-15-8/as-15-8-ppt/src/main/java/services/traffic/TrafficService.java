package services.traffic;

import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import model.enums.TrafficType;
import model.traffic.Traffic;
import model.traffic.TrafficBriefView;
import model.traffic.TrafficFullView;
import model.traffic.TrafficUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import repositories.TrafficRepository;
import utils.TrafficUnitUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TrafficService {

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
        syncAssociation(traffic);
        setNewUnitNames(traffic);
        return convertToFullView(trafficRepository.save(traffic));
    }

    @Transactional
    public TrafficFullView updateTraffic(TrafficFullView fullView, Traffic oldTraffic) {
        /*boolean updateNames = fullView.getName().compareTo(oldTraffic.getName()) != 0;

        Traffic newTraffic = convertToTraffic(fullView);
        oldTraffic.setName(newTraffic.getName());
        syncAttachedUnits(oldTraffic.getErdiTrafficUnits(),
                newTraffic.getErdiTrafficUnits());
        syncAttachedUnits(oldTraffic.getSearchQueryTrafficUnits(),
                newTraffic.getSearchQueryTrafficUnits());
        syncAssociation(oldTraffic);

        if (updateNames) {
            getUnitStream(oldTraffic).forEach(unit -> {
                String name = StringUtils.isEmpty(unit.getName()) ?
                        TrafficUnitUtils.getNewName(oldTraffic, unit.getType()) :
                        TrafficUnitUtils.getUpdateName(oldTraffic, unit.getName());
                unit.setName(name);
            });
        } else {
            setNewUnitNames(newTraffic);
        }

        return convertToFullView(trafficRepository.save(oldTraffic));*/

        Traffic newTraffic = convertToTraffic(fullView);
        oldTraffic.setName(newTraffic.getName());

        oldTraffic.getErdiTrafficUnits().clear();
        oldTraffic.getSearchQueryTrafficUnits().clear();

        oldTraffic = trafficRepository.save(oldTraffic);

        oldTraffic.getErdiTrafficUnits().addAll(
                newTraffic.getErdiTrafficUnits());
        oldTraffic.getSearchQueryTrafficUnits().addAll(
                newTraffic.getSearchQueryTrafficUnits());

        syncAssociation(oldTraffic);
        setNewUnitNames(newTraffic);
        return convertToFullView(trafficRepository.save(oldTraffic));
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
                fullView.getSearchTemplates().stream()));
        traffic.setErdiTrafficUnits(collectUnits(
                Stream.of(fullView.getFormalErdiUnit()),
                Stream.of(fullView.getCustomErdiUnit())));
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

    private <T extends TrafficUnit> List<T> collectUnits(Stream<T> a, Stream<T> b) {
        return Stream.concat(a, b)
                .filter(Objects::nonNull)
                .filter(TrafficUnit::nonEmpty)
                .collect(Collectors.toList());
    }

    private <T extends TrafficUnit> void syncAttachedUnits(List<T> attached,
                                                           List<T> actual) {
        // to do merge contents
        attached.retainAll(actual);
        actual.removeAll(attached);
        attached.addAll(actual);
    }

    private void syncAssociation(Traffic traffic) {
        getUnitStream(traffic)
                .peek(unit -> unit.setTraffic(traffic))
                .forEach(TrafficUnit::syncContentAssociation);
    }

    private void setNewUnitNames(Traffic traffic) {
        getUnitStream(traffic)
                .filter(unit -> StringUtils.isEmpty(unit.getName()))
                .forEach(unit -> unit.setName(TrafficUnitUtils
                        .getNewName(traffic, unit.getType())));
    }

    private Stream<TrafficUnit> getUnitStream(Traffic traffic) {
        return Stream.concat(
                traffic.getErdiTrafficUnits().stream().map(TrafficUnit.class::cast),
                traffic.getSearchQueryTrafficUnits().stream().map(TrafficUnit.class::cast)
        );
    }

}
