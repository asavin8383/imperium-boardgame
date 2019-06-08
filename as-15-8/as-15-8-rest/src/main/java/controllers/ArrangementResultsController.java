package controllers;

import checkUnits.CheckUnitType;
import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import lombok.extern.slf4j.Slf4j;
import model.result.ArrangementResult;
import model.result.DetailedArrangementResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementResultRepository;
import repositories.DetailedArrangementResultRepository;

import java.sql.Blob;
import java.sql.SQLException;

/**
 * Creation date: 29.05.2019
 * Author: asavin
 * Выдача результатов проведения мероприятия на фронт
 */

@RestController
@RequestMapping(path = "/results", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class ArrangementResultsController {

    private ArrangementResultRepository arrangementResultRepo;
    private DetailedArrangementResultRepository detailedArrangementResultRepo;
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public ArrangementResultsController(ArrangementResultRepository arrangementResultRepo,
                                        DetailedArrangementResultRepository detailedArrangementResultRepo,
                                        JdbcTemplate jdbcTemplate) {
        this.arrangementResultRepo = arrangementResultRepo;
        this.detailedArrangementResultRepo = detailedArrangementResultRepo;
        this.jdbcTemplate = jdbcTemplate;
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

        String fieldName = "screenshot";
        if (isEtalon) {
            fieldName = "etalon_screenshot";
        }

        String sql = "select " + fieldName + " from portal.arrangement_results where id = ?";

        Blob blob = jdbcTemplate.queryForObject(sql, new Object[]{id}, Blob.class);
        if (blob != null) {
            try {
                return new ResponseEntity<>(blob.getBytes(1, (int) blob.length()), HttpStatus.OK);
            } catch (SQLException ex) {
                log.error("Error getting image from DB", ex);
            }
        }
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }
}
