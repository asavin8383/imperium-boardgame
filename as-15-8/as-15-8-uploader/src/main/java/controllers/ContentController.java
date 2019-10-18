package controllers;

import controllers.utils.SortingDirection;
import controllers.utils.SortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import services.ContentService;

import java.util.concurrent.Future;

@RestController
@RequestMapping(path = "/erdi", produces = MediaType.APPLICATION_JSON_VALUE)
//@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ContentController {

    private final ContentService contentService;

    @GetMapping
    public Page<ContentService.ContentView> getRelevantContent(
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {

        Pageable page = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        return contentService.getRelevantContent(page);
    }

    @Async
    @GetMapping(path = "/update")
    public Future<Void> update() {
        System.out.println("Execute method asynchronously - "
                + Thread.currentThread().getName());
        try {
            Thread.sleep(5000);
            return new AsyncResult<>(null);
        } catch (InterruptedException ignore) {
            return null;
        }
    }

}
