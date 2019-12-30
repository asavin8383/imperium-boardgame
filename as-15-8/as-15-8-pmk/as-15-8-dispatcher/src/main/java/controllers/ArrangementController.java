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
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementRepo;
import services.ResultService;

import java.util.Optional;

@RestController
@PreAuthorize("hasRole('ROLE_SYSTEM')")
@RequestMapping(path = "/arrangement", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ArrangementController {

    private final ArrangementRepo arrangementRepo;
    private final ResultService resultService;

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

    @PreAuthorize("hasAnyRole('ROLE_VIEW_RESULT','ROLE_SYSTEM')")
    @GetMapping(path = "/completion")
    public int getArrangementCompletion(@RequestParam("id") Optional<Arrangement> arrangement){

        arrangement.orElseThrow(() -> new AS_15_8_DispatcherException("Ошибка поиска! Такого мероприятия не существует."));
        Long checkUnits = arrangement.get().getCheckUnitsCount();

        if (checkUnits == null)
            throw new AS_15_8_DispatcherException("Ошибка расчёта процента выполнения мероприятия. checkUnits is null");

        Long arrangementsCount = resultService.getArrangementsCount(arrangement.get().getId());
        if (arrangementsCount == null)
            throw new AS_15_8_DispatcherException("Ошибка расчёта процента выполнения мероприятия. общее число мероприятий null");

        int percent = (int) ((arrangementsCount * 100)/checkUnits);
        return percent;
    }
}
