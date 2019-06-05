package controllers;

import checkUnits.CheckUnitType;
import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import model.result.ArrangementResult;
import model.result.DetailedArrangementResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementResultRepository;
import repositories.DetailedArrangementResultRepository;

/**
 * Creation date: 29.05.2019
 * Author: asavin
 * Выдача результатов проведения мероприятия на фронт
 */

@RestController
@RequestMapping(path = "/results", produces = MediaType.APPLICATION_JSON_VALUE)
public class ArrangementResultsController {

    private ArrangementResultRepository arrangementResultRepo;
    private DetailedArrangementResultRepository detailedArrangementResultRepo;

    @Autowired
    public ArrangementResultsController(ArrangementResultRepository arrangementResultRepo,
                                        DetailedArrangementResultRepository detailedArrangementResultRepo) {
        this.arrangementResultRepo = arrangementResultRepo;
        this.detailedArrangementResultRepo = detailedArrangementResultRepo;
    }

    @PreAuthorize("hasAnyRole('ROLE_OPERATOR', 'ROLE_ADMIN')")
    @GetMapping
    public Page<ArrangementResult> findList(
            @RequestParam Long arrangementId,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String checkUnitValue,
            @RequestParam(required = false)CheckUnitType checkUnitType,
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        return arrangementResultRepo.findPage(id, arrangementId, checkUnitValue, page, checkUnitType);
    }

    @GetMapping(path = "/screenshot", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    ResponseEntity<byte[]> getScreenshot(@RequestParam Long id){
        return receiveScreenshotFromDB(id, false);
    }

    @GetMapping(path = "/etalon_screenshot", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    ResponseEntity<byte[]> getEtalonScreenshot(@RequestParam Long id){
        return receiveScreenshotFromDB(id, true);
    }

    @PreAuthorize("hasAnyRole('ROLE_OPERATOR', 'ROLE_ADMIN')")
    @GetMapping(path = "/details")
    public ResponseEntity<DetailedArrangementResult> getDetails(@RequestParam Long id){
        return detailedArrangementResultRepo.findById(id)
                .map(detailedArrangementResult -> new ResponseEntity<>(detailedArrangementResult, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NO_CONTENT));
    }

    private ResponseEntity<byte[]> receiveScreenshotFromDB(Long id, boolean isEtalon){
        return arrangementResultRepo.findById(id)
                .map(arrangementResult -> {
                    byte[] screenshot;
                    if (isEtalon){
                        screenshot = arrangementResult.getEtalonScreenshot();
                    } else {
                        screenshot = arrangementResult.getScreenshot();
                    }
                    if (screenshot != null){
                        return new ResponseEntity<>(screenshot, HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>(new byte[0], HttpStatus.NO_CONTENT);
                    }
                })
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NO_CONTENT));
    }
}
