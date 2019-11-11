package controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Result;
import model.ResultScreenShot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import repositories.ResultRepo;
import repositories.ResultScreenShotRepo;
import rest.ActCheckResult;
import services.ActService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;


@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/act")
@Slf4j
public class ActController {

    private final ActService actService;
    private final ResultRepo resultRepo;
    private final ResultScreenShotRepo resultScreenShotRepo;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @GetMapping(path = "/create")
    @PreAuthorize("hasAnyRole('ROLE_OPERATOR')")
    public ResponseEntity<Void> createAct(Long arrangementId){
        boolean created = actService.createAct(arrangementId);
        return new ResponseEntity<>(created ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(path = "/checkResult", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<ActCheckResult> getActCheckResult(Long arrangementId){
        log.info("Подготовка результатов мероприятия для акта. ID мероприятия: {}", arrangementId);
        return Flux.fromIterable(
                    resultRepo.findAllByArrangementId(arrangementId))
                .map(this::createActCheckResult);
    }

    @GetMapping(path = "/screenshots", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getActScreenshots(Long arrangementId,
                                          @RequestParam(defaultValue = "10") Long maxCountScreenShots) {
        log.info("Подготовка скриншотов для акта. ID мероприятия: {}", arrangementId);
        PageRequest page = PageRequest.of(0, maxCountScreenShots.intValue());
        List<ResultScreenShot> screenShots = resultScreenShotRepo
                .findAllByArrangementId(arrangementId, page);

        return Flux.fromIterable(screenShots)
                .map(this::createActBase64Screenshot)
                .filter(Objects::nonNull);
    }

    private ActCheckResult createActCheckResult(Result result){
        ActCheckResult checkResult = new ActCheckResult();
        checkResult.setCheckResultId(result.getId());
        checkResult.setCheckUnitType(result.getCheckUnitType());
        checkResult.setCheckUnitValue(result.getCheckUnitValue());

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
