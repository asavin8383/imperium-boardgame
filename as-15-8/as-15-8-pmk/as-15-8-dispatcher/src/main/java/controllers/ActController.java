package controllers;

import enums.CheckUnitJobResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.NmapDetailResult;
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
import repositories.NmapDetailResultRepo;
import repositories.ResultRepo;
import repositories.ResultScreenShotRepo;
import rest.ActAttachment;
import rest.ActCheckResult;
import restapi.ArrangementStatusProducer;
import services.ActService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


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
    private final NmapDetailResultRepo nmapDetailResultRepo;
    private final ArrangementStatusProducer arrangementStatusProducer;

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
        arrangementStatusProducer.changeArrangemetStatusToActSentPPT(arrangementId);
        return new ResponseEntity<>(created ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(path = "/checkResult", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<List<ActCheckResult>> getActCheckResult(Long arrangementId){
        log.info("Подготовка результатов мероприятия для акта. ID мероприятия: {}", arrangementId);
        List<CheckUnitJobResult> resultFilter = Arrays.asList(
                CheckUnitJobResult.FORBIDDEN_CONTENT_DETECTED,
                CheckUnitJobResult.COMPLETED);
        List<ActCheckResult> result = resultRepo.findResultsForAct(arrangementId, resultFilter).stream()
                .map(this::createActCheckResult).collect(Collectors.toList());
        return Flux.fromIterable(packListToLists(result, 1000));
    }

    private <T>List<List<T>> packListToLists(List<T> list, int subListSize) {
        final AtomicInteger counter = new AtomicInteger();
        return new ArrayList<>(list.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / subListSize))
                .values());
    }

    @GetMapping(path = "/screenshots", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<List<ActAttachment>> getActScreenshots(Long arrangementId) {
        log.info("Подготовка скриншотов для акта. ID мероприятия: {}", arrangementId);
        List<CheckUnitJobResult> resultFilter = Collections.singletonList(CheckUnitJobResult.FORBIDDEN_CONTENT_DETECTED);
        PageRequest page = PageRequest.of(0, maxCountActScreenShots.intValue());
        List<Result> results = resultRepo.findCheckedResultsForAct(arrangementId, resultFilter);
        if(results.size() == 0) {
            results = resultRepo.findResultsForAct(arrangementId, resultFilter, page);
        }

        if(results.size() == 0) {
            return Flux.empty();
        }

        List<Long> resultIds = results.stream()
                .mapToLong(Result::getId)
                .boxed()
                .collect(Collectors.toList());

        List<ActAttachment> result = resultScreenShotRepo.findByResultIds(resultIds)
                .stream()
                .map(this::createActBase64Screenshot).collect(Collectors.toList());

        List<ActAttachment> nmap = nmapDetailResultRepo.findByResultIds(resultIds)
                .stream()
                .map(this::createActNmapLog).collect(Collectors.toList());

       result.addAll(nmap);
       return Flux.fromIterable(packListToLists(result, 1000));
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

    private ActAttachment createActBase64Screenshot(ResultScreenShot resultScreenShot){
        byte[] screen = resultScreenShot.getScreenshot();
        if (screen != null && screen.length > 0)
            return new ActAttachment(
                    resultScreenShot.getId(),
                    ActAttachment.ActAttachmentType.SCREENSHOT,
                    Base64.getEncoder().encodeToString(screen)
            );
        return null;
    }

    private ActAttachment createActNmapLog(NmapDetailResult nmapResult){
        return new ActAttachment(
                nmapResult.getId(),
                ActAttachment.ActAttachmentType.NMAP_LOG,
                nmapResult.getLog()
        );
    }

    private Date ldt2date(LocalDateTime ldt){
        if (ldt == null)
            return null;
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }
}
