package controllers;

import analysis.CheckUnitResult;
import analysis.NMapAnalysisJobResult;
import checkUnits.CheckUnitType;
import com.fasterxml.jackson.annotation.JsonView;
import controllers.helpers.SortingHelper;
import enums.CheckUnitJobResult;
import enums.SortingDirection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.DetailResult;
import model.Result;
import model.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import services.AnalysisResultServiceFactory;
import services.DetailResultService;
import services.ResultsKafkaService;

import java.util.List;

/**
 * Creation date: 29.05.2019
 * Author: asavin
 * Выдача результатов проведения мероприятия на фронт
 */

@RestController
@RequestMapping(path = "/results", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ResultsController {

    private final ResultsKafkaService resultService;

    @PreAuthorize("hasRole('ROLE_VIEW_RESULT')")
    @GetMapping
    @JsonView(Views.Brief.class)
    public Page<Result> findList(
            @RequestParam Long arrangementId,
            @RequestParam(required = false) List<CheckUnitJobResult> checkUnitJobResults,
            @RequestParam(required = false) List<CheckUnitType> checkUnitTypes,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        return resultService.getArrangementResults(
                arrangementId,
                checkUnitJobResults,
                checkUnitTypes,
                query,
                sortingDirection,
                sortingColumn,
                pageable);
    }

    @GetMapping(value = "/ids", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Long> getListJobIds(@RequestParam Long arrangementId) {
        return Flux.fromIterable(resultService
            .getArrangementResultIds(arrangementId));
    }

    @GetMapping(path = "/screenshot", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    ResponseEntity getScreenshot(@RequestParam Long arrangementId, @RequestParam Long id){
        return resultService.getScreenshot(arrangementId, id)
            .map(screen -> ResponseEntity.ok(screen.getScreenshot()))
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping(path = "/etalon_screenshot", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    ResponseEntity getEtalonScreenshot(@RequestParam Long arrangementId, @RequestParam Long id){
        return resultService.getScreenshot(arrangementId, id)
                .map(screen -> ResponseEntity.ok(screen.getEtalonScreenshot()))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping(path = "/nmap_log", produces = MediaType.TEXT_PLAIN_VALUE)
    public @ResponseBody ResponseEntity getNmapLog(@RequestParam Long arrangementId, @RequestParam Long id){
        return resultService.getArrangementResult(arrangementId, id)
                .map(checkUnitResult -> {
                    if(checkUnitResult instanceof NMapAnalysisJobResult)
                        return ResponseEntity.ok(((NMapAnalysisJobResult)checkUnitResult).getNmapLog());
                    else
                        return ResponseEntity.noContent().build();
                }).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PreAuthorize("hasRole('ROLE_VIEW_RESULT')")
    @GetMapping("/details")
    public ResponseEntity<? extends DetailResult> getPasdDetails(@RequestParam Long arrangementId, @RequestParam Long id){
        return resultService.getArrangementResult(arrangementId, id)
                .map(checkUnitResult -> {
                    DetailResultService<? super CheckUnitResult, ? extends DetailResult> service = AnalysisResultServiceFactory.getService(checkUnitResult.getClass());
                    return ResponseEntity.ok(service.create(checkUnitResult));
                }).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping(path = "/check_unit_types")
    public List<CheckUnitType> getCheckUnitTypes(@RequestParam Long arrangementId){
        return resultService.getDictinctCheckUnitTypes(arrangementId);
    }

    @GetMapping(path = "/results")
    public List<CheckUnitJobResult> getResults(@RequestParam Long arrangementId){
        return resultService.getDictinctCheckUnitResults(arrangementId);
    }
}
