package controllers;

import com.fasterxml.jackson.annotation.JsonView;
import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_Exception;
import lombok.extern.slf4j.Slf4j;
import model.Views;
import model.task.Arrangement;
import model.task.ArrangementItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementItemRepository;
import repositories.ArrangementRepository;

import java.util.List;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 */

@RestController
@RequestMapping(path = "/arrangement_items", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@Slf4j
public class ArrangementItemController {

    private ArrangementItemRepository arrangementItemRepo;
    private ArrangementRepository arrangementRepo;


    @Autowired
    public ArrangementItemController(ArrangementItemRepository arrangementItemRepo,
                                     ArrangementRepository arrangementRepo) {
        this.arrangementItemRepo = arrangementItemRepo;
        this.arrangementRepo = arrangementRepo;
    }

    @GetMapping
    public Page<ArrangementItem> findList(
            @RequestParam(required = false) Long arrangementId,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        return arrangementItemRepo.findPage(arrangementId, id, page);
    }

    @PostMapping
    @JsonView(Views.Id.class)
    public  ResponseEntity<ArrangementItem> postArrangementItem(@RequestBody ArrangementItem arrangementItem){
        return arrangementRepo.findEditableArrangement(arrangementItem.getArrangement().getId())
            .map(arrangement -> new ResponseEntity<>(arrangementItemRepo.save(arrangementItem), HttpStatus.CREATED))
            .orElseThrow(() -> {
                log.error("Error creating arrangement item. Arrangement is not editable. id: " + arrangementItem.getArrangement().getId());
                return new AS_15_8_Exception("Error creating arrangement item. Arrangement is not editable. id: " + arrangementItem.getArrangement().getId());
            });
    }

    @PostMapping(path = "/upload")
    public ResponseEntity<Arrangement> postArrangementItems(@RequestParam Long arrangementId, @RequestBody List<ArrangementItem> arrangementItems){
        return arrangementRepo.findById(arrangementId)
            .map(arrangement -> {
                arrangementItems.forEach(arrangementItem -> {
                    arrangementItem.setArrangement(arrangement);
                    arrangementItemRepo.save(arrangementItem);
                });
                return new ResponseEntity<>(arrangement, HttpStatus.OK);
            }
            )
            .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NO_CONTENT));
    }
}
