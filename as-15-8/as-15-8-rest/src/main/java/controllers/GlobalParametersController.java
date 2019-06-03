package controllers;

import controllers.helpers.SortingHelper;
import enums.AccessToolParameters;
import enums.SortingDirection;
import model.parameters.GlobalParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.GlobalParametersRepository;

/**
 * Creation date: 03.06.2019
 * Author: asavin
 */

@RestController
@RequestMapping(path = "/parameters", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class GlobalParametersController {

    private GlobalParametersRepository globalParametersRepo;

    @Autowired
    public GlobalParametersController(GlobalParametersRepository globalParametersRepo) {
        this.globalParametersRepo = globalParametersRepo;
    }

    @GetMapping
    public Page<GlobalParameter> findList(
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        return globalParametersRepo.findAll(page);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public GlobalParameter postGlobalParameter(GlobalParameter globalParameter){
        return globalParametersRepo.save(globalParameter);
    }

    @PutMapping
    public GlobalParameter putGlobalParameter(
            @RequestParam String key,
            @RequestParam String value) {
        return globalParametersRepo.findById(key)
            .map(globalParameter -> {
                globalParameter.setValue(value);
                return globalParametersRepo.save(globalParameter);
            }).orElseGet(() -> {
                GlobalParameter globalParameter = new GlobalParameter();
                globalParameter.setKey(AccessToolParameters.valueOf(key));
                globalParameter.setValue(value);
                return globalParametersRepo.save(globalParameter);
        });
    }


}
