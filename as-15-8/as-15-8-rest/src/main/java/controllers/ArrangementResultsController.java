package controllers;

import checkUnits.CheckUnitType;
import model.result.ArrangementResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import repositories.ArrangementResultRepositoryAdvanced;

/**
 * Creation date: 29.05.2019
 * Author: asavin
 * Выдача результатов проведения мероприятия на фронт
 */

@RestController
@RequestMapping(path = "/results", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR', 'ROLE_ADMIN')")
public class ArrangementResultsController {

    private ArrangementResultRepositoryAdvanced arrangementResultRepoAdvanced;

    public ArrangementResultsController(ArrangementResultRepositoryAdvanced arrangementResultRepoAdvanced) {
        this.arrangementResultRepoAdvanced = arrangementResultRepoAdvanced;
    }

    @GetMapping
    public Page<ArrangementResult> findList(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long arrangementId,
            @RequestParam(required = false) String checkUnitValue,
            @RequestParam(required = false)CheckUnitType checkUnitType,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, Sort.by("id").ascending());
        return arrangementResultRepoAdvanced.findPage(id, arrangementId, checkUnitValue, page, checkUnitType);
    }
}
