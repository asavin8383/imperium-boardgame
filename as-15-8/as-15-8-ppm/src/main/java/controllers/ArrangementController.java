package controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementRepo;

/**
 * Created by san
 * Date: 31.10.2019
 */
@RestController
@RequestMapping(path = "/arrangements", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_={@Autowired})
@Slf4j
public class ArrangementController {

    private final ArrangementRepo arrangementRepo;

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Arrangement replaceFormalTask(@RequestBody Arrangement newArrangement, @RequestParam("id") Arrangement arrangement) {
        if(arrangement != null) {
            BeanUtils.copyProperties(newArrangement, arrangement, "id");
        } else {
            arrangement = newArrangement;
        }
        return arrangementRepo.save(arrangement);
    }

}
