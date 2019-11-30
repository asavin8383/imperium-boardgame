package controllers;

import checkUnits.CheckUnit;
import controllers.utils.SortingDirection;
import controllers.utils.SortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.controller.SearchErdiStatus;
import model.projection.ContentView;
import model.rest.control.PodState;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ContentHistoryRepository;
import rest.ActRequest;
import rest.ResponseStatusString;
import restapi.ErdiRestClient;
import services.ActService;
import services.ContentService;
import services.InfoService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
//@PreAuthorize("hasAnyRole('ROLE_SYSTEM', 'ROLE_OPERATOR')")
@Slf4j
public class ContentController {

    private final ContentService contentService;
    private final ErdiRestClient erdiRestClient;
    private final InfoService infoService;
    private final ContentHistoryRepository contentHistoryRepo;

    @GetMapping(path = "/erdi")
    @PreAuthorize("hasAnyRole('ROLE_OPERATOR')")
    public ResponseEntity<Page<ContentView>> getRelevantContent(
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String query) {

        if (!erdiRestClient.getIsLoading()) {
            Pageable pageable = PageRequest.of(pageNumber, pageSize,
                    SortingHelper.createSorting(sortingDirection, sortingColumn));
            Page<ContentView> pageContent =
                    contentService.getFormalErdiView(query, pageable);
            return new ResponseEntity<>(pageContent, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>((Page<ContentView>) null, HttpStatus.ACCEPTED);
        }
    }

    @GetMapping(path = "/erdi/single")
    //@PreAuthorize("hasAnyRole('ROLE_OPERATOR')")
    public Optional<ContentView> getContentById(
            @RequestParam Long id) {
        return contentService.getFormalErdiView(id);
    }

    @GetMapping(path = "/check_erdi", produces = MediaType.APPLICATION_JSON_VALUE)
    public SearchErdiStatus checkErdi(@RequestParam(defaultValue = "") String url) {
        return infoService.searchCheckUnit(url);
    }

    @GetMapping(path = "/erdi/expired", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM')")
    public Boolean isExpired(@RequestParam Long id) throws ParseException {
        //Добавленные менее чем за сутки не нужны
        Date restrictionDate = DateUtils.addHours(new Date(), -24);
        Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse("3000-01-01");
        return contentHistoryRepo.checkExpired(id, restrictionDate, endDate);
    }

    @GetMapping(path = "/update_erdi")
    @PreAuthorize("hasAnyRole('ROLE_OPERATOR')")
    public ResponseEntity<String> update() {
        if (!erdiRestClient.getIsLoading()){
            CompletableFuture.runAsync(erdiRestClient::startUpdateErdi);
            return new ResponseEntity<>("Началась загрузка ЕРДИ", HttpStatus.OK);
        }
        return new ResponseEntity<>("Загрузка данных в процессе", HttpStatus.PROCESSING);
    }

    @GetMapping("/get_update_date")
    @PreAuthorize("hasAnyRole('ROLE_OPERATOR')")
    public String getUpdateDate() {
        return erdiRestClient.getUpdateDate();
    }

    @GetMapping("/get_state")
    @PreAuthorize("hasAnyRole('ROLE_OPERATOR')")
    public PodState getState() throws ParseException {
        return erdiRestClient.getLoadState();
    }

    @GetMapping("/remove_content_version_to")
    @PreAuthorize("hasAnyRole('ROLE_OPERATOR')")
    public void removeLastContentVersion(@RequestParam int version) {
        erdiRestClient.removeVersionTo(version);
    }

    @GetMapping("/erdi/checkUnits")
    //@PreAuthorize("hasAnyRole('ROLE_SYSTEM')")
    public ResponseEntity<List<CheckUnit>> getCheckUnits(@RequestParam("id") Long contentId){
        List<CheckUnit> checkUnits = contentService.getActualCheckUnits(contentId).stream()
            .map(contentCheckUnit -> new CheckUnit(contentId, contentCheckUnit.getCheckUnitType(), contentCheckUnit.getCheckUnitValue()))
            .collect(Collectors.toList());
        if(checkUnits.size() > 0)
            return ResponseEntity.ok(checkUnits);
        else
            return ResponseEntity.badRequest().build();
    }

}
