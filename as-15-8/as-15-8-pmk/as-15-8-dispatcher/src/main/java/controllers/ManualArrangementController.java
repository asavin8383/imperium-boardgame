package controllers;

import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import model.Result;
import model.ResultScreenShot;
import model.enums.ArrangementStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementRepo;
import repositories.ResultRepo;
import repositories.ResultScreenShotRepo;
import restapi.ArrangementRestApi;
import services.ArrangementService;

import java.time.LocalDateTime;

@RestController
@RequestMapping(path = "/manual_arrangement", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_VIEW_RESULT')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ManualArrangementController {

    private final ArrangementService arrangementService;
    private final ResultRepo resultRepo;
    private final ResultScreenShotRepo resultScreenShotRepo;
    private final ArrangementRepo arrangementRepo;
    private final ArrangementRestApi arrangementRestApi;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    public ResponseEntity postManualArrangementFromPPT(@RequestBody Long arrangementId) {
        return arrangementService.createManualArrangement(arrangementId);
    }

    @GetMapping
    public ResponseEntity getManualArrangementResults(@RequestParam(required = false) SortingDirection sortingDirection,
                                                      @RequestParam(required = false) String sortingColumn,
                                                      @RequestParam(defaultValue = "0") int pageNumber,
                                                      @RequestParam(defaultValue = "10") int pageSize,
                                                      @RequestParam("arrangementId") Arrangement arrangement) {
        if (arrangement != null) {
            Pageable pageable = PageRequest.of(pageNumber, pageSize,
                    SortingHelper.createSorting(sortingDirection, sortingColumn));
            Page<Result> results = resultRepo.findAllByArrangementId(arrangement.getId(), pageable);
            return ResponseEntity.ok().body(results);
        } else return ResponseEntity.badRequest().body("Такое мероприятие не найдено в БД");

    }

    @PutMapping
    public ResponseEntity editResult(@RequestBody Result newResult,
                                     @RequestParam("resultId") Result result) {
        if (result != null) {
            result.setStartDate(LocalDateTime.now());
            result.setUserDescription(newResult.getUserDescription());
            result.setEndDate(LocalDateTime.now());
            result.setResult(newResult.getResult());

            return ResponseEntity.ok().body(resultRepo.save(result));
        } else {
            return ResponseEntity.badRequest().body("Результат не найден в БД");
        }
    }

    @PutMapping(path = "/screenshot")
    public ResponseEntity saveScreenshot(@RequestBody byte[] screenshot,
                                         @RequestParam("resultId") Result result) {
        if (result != null) {
            ResultScreenShot resultScreenShot = new ResultScreenShot();
            resultScreenShot.setId(result.getId());
            resultScreenShot.setResult(result);
            resultScreenShot.setScreenshot(screenshot);
            return ResponseEntity.ok().body(resultScreenShotRepo.save(resultScreenShot));
        } else {
            return ResponseEntity.badRequest().body("Результат не найден в БД");
        }
    }

    @PutMapping(path = "/finish_arrangement")
    public ResponseEntity finishArrangement(@RequestParam("id") Arrangement arrangement) {
        if (arrangement != null) {
            arrangement.setStatus(ArrangementStatus.FINISHED);
            if (arrangementRestApi.sendStatusNotificationToPPT(arrangement.getId(), true))
                return ResponseEntity.ok().body(arrangementRepo.save(arrangementRepo.save(arrangement)));
            else return ResponseEntity.badRequest().body("Ошибка отправки статуса меропрития в ППТ");
        } else {
            return ResponseEntity.badRequest().body("Мероприятие не найдено в БД");
        }
    }

    @GetMapping(path = "/screenshot")
    public ResponseEntity finishArrangement(@RequestParam("resultId") Result result) {
        if (result != null) {
            ResultScreenShot resultScreenshot = resultScreenShotRepo.findByResultId(result.getId());
            return ResponseEntity.ok().body(resultScreenshot.getScreenshot());
        } else {
            return ResponseEntity.badRequest().body("Результат не найден в БД");
        }
    }

}
