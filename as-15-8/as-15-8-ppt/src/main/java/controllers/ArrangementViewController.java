package controllers;

import lombok.RequiredArgsConstructor;
import model.task.ArrangementView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementViewRepo;

import java.security.Principal;
import java.util.List;

/**
 * Creation date: 15.06.2019
 * Author: asavin
 */
@RestController
@RequestMapping(path = "/arrangement_views", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_OPERATOR')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ArrangementViewController {

    private final ArrangementViewRepo arrangementViewRepo;

    @GetMapping
    public List<ArrangementView> findList(@RequestParam boolean viewed, Principal principal){
        return arrangementViewRepo.findAllByOperatorAndViewed(principal.getName(), viewed);
    }

    @PutMapping
    public ArrangementView updateView(@RequestParam("id") ArrangementView arrangementView){
        arrangementView.setViewed(true);
        return arrangementViewRepo.save(arrangementView);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void deleteView(@RequestParam("id") ArrangementView arrangementView){
        arrangementViewRepo.delete(arrangementView);
    }

}
