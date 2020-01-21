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
import services.ResultsKafkaService;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@PreAuthorize("hasRole('ROLE_SYSTEM')")
@RequestMapping(path = "/arrangement", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ArrangementController {

    private final ArrangementRepo arrangementRepo;
    private final ResultService resultService;
    private final ResultsKafkaService resultsKafkaService;

    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('ROLE_VIEW_RESULT')")
    public void saveResults(@RequestParam Long arrangementId) {
        CompletableFuture.runAsync(() -> resultService.saveArrangementResults(arrangementId));
    }

    @PostMapping
    public ResponseEntity<?> postUserResultFromPPM(@RequestBody Optional<ArrangementToExecution> arrangementToExecution) {
        arrangementToExecution.orElseThrow(()-> new AS_15_8_DispatcherException("Arrangement полученный из ППМ is null"));
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
    public long getArrangementCompletion(@RequestParam("id") Optional<Arrangement> arrangement){
        return arrangement
        .map(arr -> {
            Long checkUnits = arrangement.get().getCheckUnitsCount();

            if (checkUnits == null)
                throw new AS_15_8_DispatcherException("Ошибка расчёта процента выполнения мероприятия. checkUnits is null");

            long arrangementsCount = resultsKafkaService.getResultsCount(arrangement.get().getId());
            return arrangementsCount * 100 / checkUnits;
        }).orElse(0L);
    }
}
