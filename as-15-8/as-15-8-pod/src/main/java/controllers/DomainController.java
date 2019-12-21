package controllers;

import controllers.utils.SortingDirection;
import controllers.utils.SortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.scheme.Domain;
import model.scheme.DomainMask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.DomainMaskRepo;
import repositories.DomainRepo;

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

public class DomainController {

    private final DomainMaskRepo domainMaskRepo;
    private final DomainRepo domainRepo;

    @GetMapping(path = "/domains")
    public Set<String> getDomainsByMask(@RequestParam String domainMask){
        return domainRepo.getDomainsByMaskId(domainMask);
    }

    public DomainMask getDomainMaskById(@RequestParam Long id){
        return domainMaskRepo.getOne(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DomainMask postDomainMask(@RequestBody DomainMask domainMask) {
        return domainMaskRepo.save(domainMask);

    }

    @PutMapping
    public DomainMask replaceDomainMask(@RequestBody DomainMask newDomainMaskItem, @RequestParam("id") DomainMask existingDomainMaskItem){
        if (existingDomainMaskItem == null) {
            return domainMaskRepo.save(newDomainMaskItem);
        } else {
            return domainMaskRepo.save(replaceFields(newDomainMaskItem, existingDomainMaskItem));
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    public void deleteDomainMask(@RequestParam("id") DomainMask domainMask) {
        domainMaskRepo.delete(domainMask);
    }

    @GetMapping(path = "/all")
    public Page<DomainMask> findAllDomainMasks(
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "30") int pageSize,
            @RequestParam(required = false, defaultValue = "") String domainMask) {
        PageRequest page = PageRequest.of(pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        if (domainMask.isEmpty())
            return domainMaskRepo.findDomainMasksPage(page);
        else return domainMaskRepo.findDomainMasksPage(domainMask, page);
    }

    private DomainMask replaceFields(DomainMask newDomainMask, DomainMask storedDomainMask){
        storedDomainMask.setDomainMask(newDomainMask.getDomainMask());
        return storedDomainMask;
    }

}
