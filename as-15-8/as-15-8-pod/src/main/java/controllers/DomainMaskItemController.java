package controllers;

import controllers.enums.UploadingState;
import controllers.utils.SortingDirection;
import controllers.utils.SortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.scheme.DomainMaskItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.DomainMaskItemRepo;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by san
 * Date: 13.11.2019
 */

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@PreAuthorize("hasRole('ROLE_MANAGE_DOMAINS')")
@RequestMapping(path = "/domain-masks")

public class DomainMaskItemController {

    private final DomainMaskItemRepo domainMaskItemRepo;
    private UploadingState state = UploadingState.ACTIVE;

    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    @GetMapping
    public Set<String> getDomainMaskItems(@RequestParam String mask){
        return domainMaskItemRepo.findAllByDomainMask(mask)
                .stream()
                .map(DomainMaskItem::getDomainMaskItem)
                .collect(Collectors.toSet());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DomainMaskItem postDomainMask(@RequestBody DomainMaskItem domainMaskItem) {
        return domainMaskItemRepo.save(domainMaskItem);

    }

    @PutMapping
    public DomainMaskItem replaceDomainMask(@RequestBody DomainMaskItem newDomainMaskItem, @RequestParam("mask") DomainMaskItem existingDomainMaskItem){
        if (existingDomainMaskItem == null) {
            return domainMaskItemRepo.save(newDomainMaskItem);
        } else {
            return domainMaskItemRepo.save(replaceFields(newDomainMaskItem, existingDomainMaskItem));
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    public void deleteDomainMask(@RequestParam("id") DomainMaskItem domainMaskItem) {
        domainMaskItemRepo.delete(domainMaskItem);
    }

    @GetMapping(path = "/all")
    public Page<DomainMaskItem> findAllDomainMaskItems(
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "30") int pageSize,
            @RequestParam(required = false, defaultValue = "") String domainMask) {
        PageRequest page = PageRequest.of(pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        if (domainMask.isEmpty())
            return domainMaskItemRepo.findPage(page);
        else return domainMaskItemRepo.findPage(domainMask, page);
    }

    private DomainMaskItem replaceFields(DomainMaskItem newDomain, DomainMaskItem storedDomain){
        storedDomain.setDomainMask(newDomain.getDomainMask());
        storedDomain.setDomainMaskItem(newDomain.getDomainMaskItem());
        return storedDomain;
    }

}
