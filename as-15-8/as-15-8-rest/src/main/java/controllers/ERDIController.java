package controllers;

import model.erdi.ERDI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import repositories.ERDIRepositoryAdvanced;

import java.time.LocalDateTime;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 */

@RestController
@RequestMapping(path = "/erdi", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
public class ERDIController {

    private ERDIRepositoryAdvanced erdiRepoAdvanced;

    @Autowired
    public ERDIController(ERDIRepositoryAdvanced erdiRepoAdvanced) {
        this.erdiRepoAdvanced = erdiRepoAdvanced;
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
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, Sort.by("id").ascending());
        return erdiRepoAdvanced.findPage(id, arrangementId, organization, blocktype, page);
    }
}
