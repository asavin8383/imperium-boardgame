package controllers;

import enums.CheckUnitJobResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Result;
import model.ResultScreenShot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import repositories.ResultRepo;
import repositories.ResultScreenShotRepo;
import rest.ActCheckResult;
import services.ActService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;


@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/act")
@Slf4j
public class ActController {

    @Value("${act.screenshotes.max-count}")
    private Long maxCountActScreenShots;

    private final ActService actService;
    private final ResultRepo resultRepo;
    private final ResultScreenShotRepo resultScreenShotRepo;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @PreAuthorize("hasRole('ROLE_SEND_ACT_BY_HAND')")
    @PutMapping(path = "/check")
    public ResponseEntity checkResultForAct(@RequestParam Long resultId, @RequestParam boolean checked){
        return resultRepo.findById(resultId)
                .map(result -> {
                    if(result.getResult().equals(CheckUnitJobResult.FORBIDDEN_CONTENT_DETECTED)) {

                        result.setCheckForAct(checked);
                        resultRepo.save(result);
                        return ResponseEntity.ok().build();
                    } else {
                        return ResponseEntity.badRequest().body("В акт возможно отправлять скриншоты только с обнаруженным запрещенным контентом");
                    }
                }).orElseGet(() -> ResponseEntity.badRequest().body("Ошибка! Результат выполнения проверки не найден по ID: " + resultId));
    }

    @GetMapping(path = "/create")
    @PreAuthorize("hasAnyRole('ROLE_SEND_ACT_BY_HAND')")
    public ResponseEntity<Void> createAct(Long arrangementId){
        boolean created = actService.createAct(arrangementId);
        return new ResponseEntity<>(created ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(path = "/checkResult", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ActCheckResult> getActCheckResult(Long arrangementId){
        log.info("Подготовка результатов мероприятия для акта. ID мероприятия: {}", arrangementId);
        List<CheckUnitJobResult> resultFilter = Arrays.asList(
                CheckUnitJobResult.FORBIDDEN_CONTENT_DETECTED,
                CheckUnitJobResult.COMPLETED);
        return Flux.fromIterable(
                    resultRepo.findResultsForAct(arrangementId, resultFilter))
                .map(this::createActCheckResult);
    }

    @GetMapping(path = "/screenshots", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getActScreenshots(Long arrangementId) {
        log.info("Подготовка скриншотов для акта. ID мероприятия: {}", arrangementId);
        List<CheckUnitJobResult> resultFilter = Collections.singletonList(CheckUnitJobResult.FORBIDDEN_CONTENT_DETECTED);
        PageRequest page = PageRequest.of(0, maxCountActScreenShots.intValue());
        List<ResultScreenShot> screenShots = resultScreenShotRepo
                .findCheckedByArrangementIdAndResultIn(arrangementId, resultFilter);
        if(screenShots.size() == 0){
            screenShots = resultScreenShotRepo
                    .findByArrangementIdAndResultIn(arrangementId, resultFilter, page);
        }

        return Flux.fromIterable(screenShots)
                .map(this::createActBase64Screenshot)
                .filter(Objects::nonNull);
    }

    private ActCheckResult createActCheckResult(Result result){
        ActCheckResult checkResult = new ActCheckResult();
        checkResult.setCheckResultId(result.getId());
        checkResult.setCheckUnitType(result.getCheckUnitType());
        checkResult.setCheckUnitValue(result.getCheckUnitValue());
        checkResult.setContentId(result.getErdiId());
        checkResult.setForbiddenContentDetected(result.getResult().equals(CheckUnitJobResult.FORBIDDEN_CONTENT_DETECTED));

        Date endDate = ldt2date(result.getEndDate());
        checkResult.setDate(endDate == null ? "" : dateFormat.format(endDate));
        return checkResult;
    }

    private String createActBase64Screenshot(ResultScreenShot resultScreenShot){
        byte[] screen = resultScreenShot.getScreenshot();
        if (screen != null && screen.length > 0)
            return Base64.getEncoder().encodeToString(screen);
        return null;
    }

    private Date ldt2date(LocalDateTime ldt){
        if (ldt == null)
            return null;
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }
}
