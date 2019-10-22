package controllers;

import controllers.utils.SortingDirection;
import controllers.utils.SortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.projection.ContentView;
import model.rest.control.PodState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import restapi.ErdiRestClient;
import services.ContentService;
import utils.Utils;

import java.text.ParseException;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ContentController {

    private final ContentService contentService;
    private final ErdiRestClient erdiRestClient;

    @GetMapping(path = "/erdi")
    public ResponseEntity<Page<ContentView>> getRelevantContent(
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String query) {

        if (!erdiRestClient.getIsLoading()) {
            Pageable page = PageRequest.of(pageNumber, pageSize,
                    SortingHelper.createSorting(sortingDirection, sortingColumn));
            Page<ContentView> pageContent = contentService.getByEffDtAndQuery(
                    Utils.getEndDate(), query, page);
            return new ResponseEntity<>(pageContent, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(null, HttpStatus.PROCESSING);
        }
    }

    @GetMapping(path = "/update_erdi")
    public ResponseEntity<String> update() {
        if (!erdiRestClient.getIsLoading()){
            CompletableFuture.runAsync(erdiRestClient::startUpdateErdi);
            return new ResponseEntity<>("Началась загрузка ЕРДИ", HttpStatus.OK);
        }
        return new ResponseEntity<>("Загрузка данных в процессе", HttpStatus.PROCESSING);
    }

    @GetMapping("/get_update_date")
    public String getUpdateDate() {
        return erdiRestClient.getUpdateDate();
    }

    @GetMapping("/get_state")
    public PodState getState() throws ParseException {
        return erdiRestClient.getLoadState();
    }

    @GetMapping("/remove_content_version_to")
    public void removeLastContentVersion(@RequestParam int version) {
        erdiRestClient.removeVersionTo(version);
    }

}
