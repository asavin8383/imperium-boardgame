package controllers;

import checkUnits.CheckUnit;
import controllers.utils.SortingDirection;
import controllers.utils.SortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.controller.SearchErdiStatus;
import model.projection.ContentView;
import model.rest.control.PodState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rest.ActRequest;
import rest.ResponseStatusString;
import restapi.ErdiRestClient;
import services.ActService;
import services.ContentService;
import services.InfoService;

import java.text.ParseException;
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
    public SearchErdiStatus update(
            @RequestParam(defaultValue = "") String url
    ) {
        return infoService.searchCheckUnit(url);
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
