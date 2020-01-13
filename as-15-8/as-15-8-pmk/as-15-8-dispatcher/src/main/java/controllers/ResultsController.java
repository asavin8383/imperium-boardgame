package controllers;

import analysis.AnalysisResult;
import analysis.CheckUnitResult;
import analysis.NMapAnalysisJobResult;
import checkUnits.CheckUnitType;
import controllers.helpers.SortingHelper;
import enums.CheckUnitJobResult;
import enums.ExecutionStatus;
import enums.SortingDirection;
import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.enums.UserResult;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.*;
import services.ResultService;

import java.util.*;

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

    private final ResultService resultService;

    @PreAuthorize("hasRole('ROLE_VIEW_RESULT')")
    @GetMapping
    public Page<CheckUnitResult> findList(
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
                pageable);
    }

    @GetMapping(path = "/screenshot", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    ResponseEntity<byte[]> getScreenshot(@RequestParam Long arrangementId, @RequestParam Long id){
        CheckUnitResult checkUnitResult = resultService.getArrangementResult(arrangementId, id);
        if(checkUnitResult instanceof AnalysisResult)
            return ResponseEntity.ok(((AnalysisResult)checkUnitResult).getScreenshot());
        else
            return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/etalon_screenshot", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    ResponseEntity<byte[]> getEtalonScreenshot(@RequestParam Long arrangementId, @RequestParam Long id){
        CheckUnitResult checkUnitResult = resultService.getArrangementResult(arrangementId, id);
        if(checkUnitResult instanceof AnalysisResult)
            return ResponseEntity.ok(((AnalysisResult)checkUnitResult).getEtalonScreenshot());
        else
            return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/nmap_log", produces = MediaType.TEXT_PLAIN_VALUE)
    public @ResponseBody ResponseEntity<String> getNmapLog(@RequestParam Long arrangementId, @RequestParam Long id){
        CheckUnitResult checkUnitResult = resultService.getArrangementResult(arrangementId, id);
        if(checkUnitResult instanceof NMapAnalysisJobResult)
            return ResponseEntity.ok(((NMapAnalysisJobResult)checkUnitResult).getNmapLog());
        else
            return ResponseEntity.noContent().build();
    }

}
