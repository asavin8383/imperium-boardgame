package controllers;

import exceptions.AS_15_8_Exception;
import model.task.Arrangement;
import model.task.ArrangementItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementItemRepository;
import repositories.ArrangementItemRepositoryAdvanced;
import repositories.ArrangementRepository;
import repositories.ERDIRepository;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 */

@RestController
@RequestMapping(path = "/arrangement_items", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
public class ArrangementItemController {

    private ArrangementItemRepository arrangementItemRepo;
    private ArrangementItemRepositoryAdvanced arrangementItemRepoAdvanced;
    private ArrangementRepository arrangementRepo;
    private ERDIRepository erdiRepo;

    @Autowired
    public ArrangementItemController(ArrangementItemRepository arrangementItemRepo,
                                     ArrangementItemRepositoryAdvanced arrangementItemRepoAdvanced,
                                     ArrangementRepository arrangementRepo,
                                     ERDIRepository erdiRepo) {
        this.arrangementItemRepo = arrangementItemRepo;
        this.arrangementItemRepoAdvanced = arrangementItemRepoAdvanced;
        this.arrangementRepo = arrangementRepo;
        this.erdiRepo = erdiRepo;
    }

    @GetMapping
    public Page<ArrangementItem> findList(
            @RequestParam(required = false) Long arrangementId,
            @RequestParam(required = false) Long id,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, Sort.by("id").ascending());
        return arrangementItemRepoAdvanced.findPage(arrangementId, id, page);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArrangementItem postArrangementItem(@RequestParam Long arrangementId,
                                               @RequestParam Long erdiId){
        return arrangementRepo.findById(arrangementId)
                .map(arrangement -> erdiRepo.findById(erdiId)
                        .map(erdi -> {
                            ArrangementItem arrangementItem = new ArrangementItem();
                            arrangementItem.setArrangement(arrangement);
                            arrangementItem.setErdi(erdi);
                            return arrangementItemRepo.save(arrangementItem);
                        }).orElseThrow(() -> new AS_15_8_Exception("Error creating arrangement Item! Erdi was not found by id: " + erdiId))
                )
                .orElseThrow(() -> new AS_15_8_Exception("Error creating arrangement Item! Arrangement was not found by id: " + arrangementId));
    }

}
