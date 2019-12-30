package controllers;

import arrangement.ArrangementToExecution;
import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import model.enums.ArrangementStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import repositories.ArrangementRepo;

import java.util.Optional;

@RestController
@PreAuthorize("hasRole('ROLE_SYSTEM')")
@RequestMapping(path = "/arrangement", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ArrangementController {

    private final ArrangementRepo arrangementRepo;

    @PostMapping
    public ResponseEntity<?> postUserResultFromPPM(@RequestBody Optional<ArrangementToExecution> arrangementToExecution) {
        arrangementToExecution.orElseThrow(()-> new AS_15_8_DispatcherException("Arrangemnt полученный из ППМ is null"));
        try {
            Arrangement arrangement = new Arrangement();
            arrangement.setId(arrangementToExecution.get().getId());
            arrangement.setCheckUnitsCount(arrangementToExecution.get().getCheckUnitsCount());
            arrangement.setStatus(ArrangementStatus.RUNNING);
            arrangementRepo.save(arrangement);
            return new ResponseEntity<>( HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>("Ошибка записи в мероприятия в репозиторий", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
