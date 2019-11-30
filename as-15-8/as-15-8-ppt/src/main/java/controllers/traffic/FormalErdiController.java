package controllers.traffic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.traffic.ErdiTrafficUnit;
import model.traffic.ErdiTrafficUnitContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import repositories.ErdiContentJoinRepository;
import webClients.PodWebClient;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/erdi/formal", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class FormalErdiController {

    private final PodWebClient podWebClient;
    private final ErdiContentJoinRepository erdiContentJoinRepository;

    @GetMapping
    public Page<ObjectNode> getFormalErdiIds(@RequestParam(required = false) SortingDirection sortingDirection,
                                             @RequestParam(required = false) String sortingColumn,
                                             @RequestParam(defaultValue = "0") int pageNumber,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam("erdiTrafficUnitId") ErdiTrafficUnit erdiTrafficUnit) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        if (erdiTrafficUnit != null) {
            Page<ErdiTrafficUnitContent> trafficUnitContents =
                    erdiContentJoinRepository
                    .findByTrafficUnit(erdiTrafficUnit, pageable);
            List<Long> contentIds = trafficUnitContents
                    .stream()
                    .map(ErdiTrafficUnitContent::getErdiId)
                    .collect(Collectors.toList());
            List<ObjectNode> erdiList = podWebClient.fetchErdi(contentIds);
            return new PageImpl<>(erdiList, pageable, trafficUnitContents.getTotalElements());
        } else {
            throw new AS_15_8_PPT_Exception("Not supported");
        }
    }

}
