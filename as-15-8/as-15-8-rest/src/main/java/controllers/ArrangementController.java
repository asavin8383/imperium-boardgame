package controllers;

import exceptions.AS_15_8_Exception;
import model.task.Arrangement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementRepository;
import repositories.ArrangementRepositoryAdvanced;
import repositories.FormalTaskRepository;

/**
 * Creation date: 21.05.2019
 * Author: asavin
 */

@RestController
@RequestMapping(path = "/arrangements", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
public class ArrangementController {

    private ArrangementRepository arrangementRepo;
    private FormalTaskRepository formalTaskRepo;
    private ArrangementRepositoryAdvanced arrangementRepoAdvanced;

    @Autowired
    public ArrangementController(ArrangementRepository arrangementRepo, FormalTaskRepository formalTaskRepo, ArrangementRepositoryAdvanced arrangementRepoAdvanced) {
        this.arrangementRepo = arrangementRepo;
        this.formalTaskRepo = formalTaskRepo;
        this.arrangementRepoAdvanced = arrangementRepoAdvanced;
    }

    @GetMapping
    public Page<Arrangement> findList(
            @RequestParam(required = false) Long formalTaskId,
            @RequestParam(required = false) Long id,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, Sort.by("id").ascending());
        return arrangementRepoAdvanced.findPage(formalTaskId, id, page);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    public Arrangement postArrangement(@RequestBody Arrangement arrangement, @RequestParam Long formalTaskId){
        return formalTaskRepo.findById(formalTaskId)
            .map(formalTask -> {
                arrangement.setFormalTask(formalTask);
                return arrangementRepo.save(arrangement);
            })
            .orElseThrow(() -> new AS_15_8_Exception("Error creating arrangement! Formal task was not found by id: " + formalTaskId));

    }

}
