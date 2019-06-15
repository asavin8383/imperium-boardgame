package controllers;

import model.task.ArrangementView;
import model.user.User;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import repositories.ArrangementViewRepo;

import java.util.List;

/**
 * Creation date: 15.06.2019
 * Author: asavin
 */
@RestController
@RequestMapping(path = "/arrangement_views", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR', 'ROLE_ADMIN')")
public class ArrangementViewController {

    private ArrangementViewRepo arrangementViewRepo;

    public ArrangementViewController(ArrangementViewRepo arrangementViewRepo) {
        this.arrangementViewRepo = arrangementViewRepo;
    }

    @GetMapping
    public List<ArrangementView> findList(@RequestParam(value = "user_id") User user,
                                          @RequestParam boolean status){
        return arrangementViewRepo.findAllByUserAndViewed(user, status);
    }
}
