package controllers;

import arrangement.ArrangementToExecution;
import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementRepo;
import services.ArrangementService;
import services.ResultService;
import services.ResultsKafkaService;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(path = "/arrangements", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ArrangementController {

    private final ArrangementRepo arrangementRepo;
    private final ResultService resultService;
    private final ResultsKafkaService resultsKafkaService;
    private final ArrangementService arrangementService;

    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('ROLE_VIEW_RESULT')")
    public void saveResults(@RequestParam Long arrangementId) {
        CompletableFuture.runAsync(() -> resultService.saveArrangementResults(arrangementId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    public ResponseEntity<?> postUserResultFromPPM(@RequestBody ArrangementToExecution arrangementToExecution) {
        try {
            arrangementService.createOrRestart(arrangementToExecution);
            return new ResponseEntity<>( HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>("Ошибка записи в мероприятия в репозиторий", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEW_RESULT')")
    @GetMapping(path = "/completion")
    public long getArrangementCompletion(@RequestParam(name = "id", required = false) Arrangement arrangement){
        return Optional.ofNullable(arrangement)
        .map(arr -> {
            Long checkUnits = arrangement.getCheckUnitsCount();

            if (checkUnits == null)
                throw new AS_15_8_DispatcherException("Ошибка расчёта процента выполнения мероприятия. checkUnits is null");

            long arrangementsCount = resultsKafkaService.getResultsCount(arrangement.getId());
            return arrangementsCount * 100 / checkUnits;
        }).orElse(0L);
    }

    @PostMapping("/stop")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM')")
    public void stopArrangement(@RequestParam Long arrangementId, @RequestParam Long version) {
        arrangementService.stopExecution(arrangementId, version);
    }

    @GetMapping("/stopped")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM')")
    public Map<Long, Set<Long>> getStoppedArrangement() {
        return arrangementService.getStoppedArrangements();
    }
}
