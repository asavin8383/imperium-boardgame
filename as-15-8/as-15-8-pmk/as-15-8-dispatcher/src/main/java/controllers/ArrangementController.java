package controllers;

import arrangement.ArrangementToExecution;
import exceptions.AS_15_8_DispatcherException;
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

    private final ResultService resultService;
    private final ResultsKafkaService resultsKafkaService;
    private final ArrangementService arrangementService;
    private final ArrangementRepo arrangementRepo;

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
        return arrangementService.getCompletionPerscent(arrangement);
    }

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

    @PutMapping("/stop")
    @PreAuthorize("hasAnyRole('ROLE_MANAGE_ARRANGEMENT')")
    public void stopArrangement(@RequestParam Long id) {
        Arrangement arrangement = arrangementRepo.findById(id).orElseThrow(() ->
                new AS_15_8_DispatcherException("Ошибка остановки мероприятия. Такое мероприятие не найдено в БД, id = " + id));
        arrangementService.stopExecution(id, arrangement.getVersion(), Reason.MANUAL);
    }

    @PutMapping("/finish")
    @PreAuthorize("hasAnyRole('ROLE_MANAGE_ARRANGEMENT')")
    public void finishArrangement(@RequestParam Long id) {
        arrangementService.finishArrangement(id);
    }

    @PutMapping("/stop_by_day_gone")
    @PreAuthorize("hasAnyRole('ROLE_MANAGE_ARRANGEMENT')")
    public void stopAllArrsByDayGone(@RequestParam Long id) {
        arrangementService.stopAllRunningArrangements(Reason.STOPPED_BY_DAY_GONE);
    }
}
