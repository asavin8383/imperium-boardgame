package controllers;

import arrangement.ArrangementToExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import model.enums.Reason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import restapi.ArrangementRestApi;
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

    private final ResultService resultService;
    private final ResultsKafkaService resultsKafkaService;
    private final ArrangementService arrangementService;
    private final ArrangementRestApi arrangementRestApi;


    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('ROLE_VIEW_RESULT')")
    public void saveResults(@RequestParam Long arrangementId) {
        CompletableFuture.runAsync(() -> resultService.saveArrangement(arrangementId));
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

            if (checkUnits == null || checkUnits == 0)
                return 0L;

            long arrangementsCount = resultsKafkaService.getResultsCount(arrangement.getId());
            return Math.min(arrangementsCount * 100 / checkUnits, 100);
        }).orElse(0L);
    }

    @PostMapping("/stop")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM')")
    public void stopArrangement(@RequestParam Long arrangementId, @RequestParam Long version) {
        arrangementService.stopExecution(arrangementId, version, Reason.MANUAL);
    }

    /*@PostMapping("/finish")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM')")
    public void finishArrangement(@RequestParam Long arrangementId) {
        arrangementRestApi.sendStatusNotificationToPPM(arrangementId, false);
    }*/

    @GetMapping("/stopped")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM')")
    public Map<Long, Set<Long>> getStoppedArrangement() {
        return arrangementService.getStoppedArrangements();
    }

    @PostMapping("/stop_all_running")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM')")
    public void stopAllRunningArrangements() {
        arrangementService.stopAllRunningArrangements(Reason.STOPPED_BY_SERVICE_MODE);
    }

    @PreAuthorize("hasRole('ROLE_MANAGE_ARRANGEMENT')" )
    @GetMapping(path = "/test_async")
    public ResponseEntity<String> testAsync(){
        resultService.longRunningTest();
        return ResponseEntity.ok("ok");
    }
}
