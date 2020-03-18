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
import org.springframework.web.multipart.MultipartFile;
import repositories.ArrangementRepo;
import repositories.ResultRepo;
import repositories.ResultScreenShotRepo;
import services.ArrangementService;

import java.io.IOException;
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
            checkArrangementStatus(result);
            result.setEndDate(LocalDateTime.now());
            result.setUserDescription(newResult.getUserDescription());
            result.setUserResult(newResult.getUserResult());
            result.setCheckForAct(newResult.isCheckForAct());

            return ResponseEntity.ok().body(resultRepo.save(result));
        } else {
            return ResponseEntity.badRequest().body("Результат не найден в БД");
        }
    }

    @PutMapping(path = "/screenshot")
    public ResponseEntity saveScreenshot(@RequestParam("file") MultipartFile file,
                                         @RequestParam("resultId") Result result) throws IOException {
        if (result != null) {
            checkArrangementStatus(result);
            ResultScreenShot resultScreenShot = new ResultScreenShot();
            resultScreenShot.setId(result.getId());
            resultScreenShot.setResult(result);
            byte[] screenshot = file.getBytes();
            if (screenshot != null && screenshot.length > 0 ) {
                resultScreenShot.setScreenshot(file.getBytes());
                resultScreenShotRepo.save(resultScreenShot);
                return ResponseEntity.ok().build();
            } else return ResponseEntity.badRequest().body("Файл со скриншотом пустой или имеет нулевой размер");
        } else {
            return ResponseEntity.badRequest().body("Результат не найден в БД");
        }
    }

    @GetMapping(path = "/screenshot", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody ResponseEntity finishArrangement(@RequestParam("resultId") Result result) {
        if (result != null) {
            if (result.isScreenshotAvailable()) {
                ResultScreenShot resultScreenshot = resultScreenShotRepo.findByResultId(result.getId());
                byte[] screenshot = resultScreenshot.getScreenshot();
                return ResponseEntity.ok().body(screenshot);
            } else return ResponseEntity.badRequest().body("У результата нет скриншота");
        } else
            return ResponseEntity.badRequest().body("Результат не найден в БД");
    }

    @PutMapping(path = "/finish_arrangement")
    public ResponseEntity finishArrangement(@RequestParam("id") Arrangement arrangement) {
        if (arrangement != null) {
            if (arrangement.getStatus().equals(ArrangementStatus.RUNNING)) {
                arrangement.setStatus(ArrangementStatus.FINISHED);
                if (arrangementService.finishArrangement(arrangement.getId())) {
                    arrangementRepo.save(arrangement);
                    return ResponseEntity.ok().body(arrangement);
                } else return ResponseEntity.badRequest().body("Ошибка отправки статуса меропрития в ППТ");
            } else return ResponseEntity.badRequest().body("Невозможно остановить мероприятие со статусом : " + arrangement.getStatus());
        } else {
            return ResponseEntity.badRequest().body("Мероприятие не найдено в БД");
        }
    }

    private ResponseEntity checkArrangementStatus(Result result) {
        if (result.getArrangement().getStatus().equals(ArrangementStatus.FINISHED))
            return ResponseEntity.badRequest().body("Мероприятие уже завершено");
        return null;
    }

}
