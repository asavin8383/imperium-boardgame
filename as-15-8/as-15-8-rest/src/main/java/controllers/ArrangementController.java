package controllers;

import model.task.Arrangement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementRepository;

import java.util.Optional;

/**
 * Creation date: 21.05.2019
 * Author: asavin
 */

@RestController
@RequestMapping(path = "/arrangements", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
public class ArrangementController {

    private ArrangementRepository arrangementRepo;

    @Autowired
    public ArrangementController(ArrangementRepository arrangementRepo) {
        this.arrangementRepo = arrangementRepo;
    }

    @GetMapping
    public Optional<Arrangement> findOne(@RequestParam Long formalTaskId, @RequestParam Long id){
        return arrangementRepo.findByFormalTaskAndId(formalTaskId, id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    public Arrangement postArrangement(@RequestBody Arrangement arrangement){
        return arrangementRepo.save(arrangement);
    }

}
