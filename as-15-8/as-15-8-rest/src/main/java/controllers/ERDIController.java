package controllers;

import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import model.erdi.ERDI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ERDIRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 */

@RestController
@RequestMapping(path = "/erdi", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
public class ERDIController {

    private ERDIRepository erdiRepo;

    @Autowired
    public ERDIController(ERDIRepository erdiRepo) {
        this.erdiRepo = erdiRepo;
    }

    @GetMapping
    public Page<ERDI> findList(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long arrangementId,
            @RequestParam(required = false) String organization,
            @RequestParam(required = false) String decisionNumber,
            @RequestParam(required = false) LocalDateTime decisionDate,
            @RequestParam(required = false) String checkUnitValue,
            @RequestParam(required = false) String blocktype,
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        return erdiRepo.findPage(id, arrangementId, organization, blocktype, page);
    }

    @GetMapping("/single/{id}")
    public ERDI erdiById(@PathVariable("id") Long id){
        Optional<ERDI> erdiOpt = erdiRepo.findById(id);
        if(erdiOpt.isPresent()){
            return erdiOpt.get();
        }
        return null;
    }
}
