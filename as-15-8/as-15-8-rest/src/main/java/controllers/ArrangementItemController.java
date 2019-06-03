package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_Exception;
import model.erdi.ERDI;
import model.task.Arrangement;
import model.task.ArrangementItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementItemRepository;
import repositories.ArrangementItemRepositoryAdvanced;
import repositories.ArrangementRepository;
import repositories.ERDIRepository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        return arrangementItemRepoAdvanced.findPage(arrangementId, id, page);
    }

    @PostMapping
    public  ResponseEntity<ArrangementItem> postArrangementItem(@RequestParam Long arrangementId, @RequestParam Long erdiId){
        return arrangementRepo.findEditableArrangement(arrangementId)
                .map(arrangement -> erdiRepo.findById(erdiId)
                        .map(erdi -> {
                            ArrangementItem arrangementItem = new ArrangementItem();
                            arrangementItem.setArrangement(arrangement);
                            arrangementItem.setErdi(erdi);
                            return new ResponseEntity<>(arrangementItemRepo.save(arrangementItem), HttpStatus.CREATED);
                        }).orElseThrow(() -> new AS_15_8_Exception("Error creating arrangement Item! Erdi was not found by id: " + erdiId))
                )
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NO_CONTENT));
    }

    @PostMapping(path = "/upload")
    public ResponseEntity<Arrangement> postArrangementItems(@RequestParam Long arrangementId, @RequestBody List<ERDI> erdiList){
        return arrangementRepo.findById(arrangementId)
                .map(arrangement -> {
                    List<ERDI> dbErdiList = arrangement.getArrangementItems().stream()
                        .map(ArrangementItem::getErdi)
                        .collect(Collectors.toList());
                    erdiList.forEach(erdi -> {
                        if (!dbErdiList.contains(erdi)){
                            ArrangementItem arrangementItem = new ArrangementItem();
                            arrangementItem.setErdi(erdi);
                            arrangementItem.setArrangement(arrangement);
                            arrangementItemRepo.save(arrangementItem);
                        }
                    });
                    return new ResponseEntity<>(arrangement, HttpStatus.OK);
                }
                )
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NO_CONTENT));
    }

    @GetMapping(path = "/download")
    public ResponseEntity<byte[]> uploadItems(@RequestParam Long arrangementId) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] bytes = objectMapper.writeValueAsBytes(
            arrangementItemRepo.findAllByArrangementId(arrangementId).stream()
            .map(arrangementItem -> objectMapper.createObjectNode().put("id", arrangementItem.getErdi().getId()))
            .collect(Collectors.toList())
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noCache().getHeaderValue());

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

}
