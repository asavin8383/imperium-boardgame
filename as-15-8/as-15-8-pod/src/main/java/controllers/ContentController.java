package controllers;

import checkUnits.CheckUnit;
import controllers.utils.SortingDirection;
import controllers.utils.SortingHelper;
import enums.ErdiStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.projection.ContentView;
import model.rest.control.UpdateErdiState;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import repositories.ContentCheckUnitRepository;
import repositories.ContentHistoryRepository;
import repositories.ContentViewRepository;
import rest.ResponseStatusString;
import restapi.ErdiRestClient;
import services.ContentService;
import services.InfoService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
//@PreAuthorize("hasAnyRole('ROLE_SYSTEM', 'ROLE_OPERATOR')")
@Slf4j
public class ContentController {

    private final ContentService contentService;
    private final ContentViewRepository contentViewRepository;
    private final ErdiRestClient erdiRestClient;
    private final InfoService infoService;
    private final ContentHistoryRepository contentHistoryRepo;
    private final ContentCheckUnitRepository contentCheckUnitRepository;

    @GetMapping(path = "/erdi")
    @PreAuthorize("hasAnyRole('ROLE_MANAGE_ERDI')")
    public ResponseEntity<Page<ContentView>> getRelevantContent(
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) List<String> categoryNames,
            @RequestParam(required = false) List<String> decisionOrgs,
            @RequestParam(required = false) List<String> infoTypeIds,
            @RequestParam(required = false) List<String> registryNames,
            @RequestParam(required = false) List<String> resourceTypes,
            @RequestParam(required = false) String resourceValue,
            @RequestParam(required = false) List<String> violationNames,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean random,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endTime,
            @RequestParam(required = false) Long visitorsCntRussiaMin,
            @RequestParam(required = false) Long visitorsCntRussiaMax,
            @RequestParam(required = false) Long visitorsCntWorldMin,
            @RequestParam(required = false) Long visitorsCntWorldMax
    ) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        Page<ContentView> pageContent =
                contentViewRepository.findPage(
                        id,
                        categoryNames,
                        decisionOrgs,
                        infoTypeIds,
                        registryNames,
                        resourceTypes,
                        resourceValue,
                        violationNames,
                        query,
                        random == null ? false : random,
                        pageable,
                        convertToLocalDateTimeMin(startTime),
                        convertToLocalDateTimeMax(endTime),
                        visitorsCntRussiaMin,
                        visitorsCntRussiaMax,
                        visitorsCntWorldMin,
                        visitorsCntWorldMax);
        return new ResponseEntity<>(pageContent, HttpStatus.OK);
    }

    @GetMapping(path = "/erdi/ids", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<List<Long>> getRelevantContentIds(

            @RequestParam(required = false) String idMask,
            @RequestParam(required = false) List<String> categoryNames,
            @RequestParam(required = false) List<String> decisionOrgs,
            @RequestParam(required = false) List<String> infoTypeIds,
            @RequestParam(required = false) List<String> registryNames,
            @RequestParam(required = false) List<String> resourceTypes,
            @RequestParam(required = false) String resourceValue,
            @RequestParam(required = false) List<String> violationNames,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endTime,
            @RequestParam(required = false) Boolean random,
            @RequestParam(required = false, defaultValue = "ASC") SortingDirection sortingDirection,
            @RequestParam(required = false, defaultValue = "includetime") String sortingColumn,
            @RequestParam(required = false) Long visitorsCntRussiaMin,
            @RequestParam(required = false) Long visitorsCntRussiaMax,
            @RequestParam(required = false) Long visitorsCntWorldMin,
            @RequestParam(required = false) Long visitorsCntWorldMax
    ) {

        if (!erdiRestClient.getIsLoading()) {

            Pageable pageable = PageRequest.of(0, 10,
                    SortingHelper.createSorting(sortingDirection, sortingColumn));

            List<List<Long>> listContent =
                    contentViewRepository.findIds(
                            idMask,
                            categoryNames,
                            decisionOrgs,
                            infoTypeIds,
                            registryNames,
                            resourceTypes,
                            resourceValue,
                            violationNames,
                            size,
                            convertToLocalDateTimeMin(startTime),
                            convertToLocalDateTimeMax(endTime),
                            random,
                            pageable,
                            visitorsCntRussiaMin,
                            visitorsCntRussiaMax,
                            visitorsCntWorldMin,
                            visitorsCntWorldMax);

            return Flux.fromIterable(listContent);
        }
        else {
            return Flux.empty();
        }
    }

    private LocalDateTime convertToLocalDateTimeMin(LocalDate dateToConvert) {
        if (dateToConvert != null)
            return LocalDateTime.of(dateToConvert, LocalDateTime.MIN.toLocalTime());
        else return null;
    }

    private LocalDateTime convertToLocalDateTimeMax(LocalDate dateToConvert) {
        if (dateToConvert != null)
            return LocalDateTime.of(dateToConvert, LocalDateTime.MAX.toLocalTime());
        else return null;
    }

    @GetMapping(path = "/erdi/resourceTypes")
    public List<String> getResourceTypes(){
        return contentViewRepository.getDistinctResourceTypes();
    }

    @GetMapping(path = "/erdi/categoryNames")
    public List<String> getCategoryNames(){
        return contentViewRepository.getDistinctCategoryNames();
    }

    @GetMapping(path = "/erdi/registryNames")
    public List<String> getRegistryNames(){
        return contentViewRepository.getDistinctRegistryNames();
    }

    @GetMapping(path = "/erdi/violationNames")
    public List<String> getViolationNames(){
        return contentViewRepository.getDistinctViolationNames();
    }

    @GetMapping(path = "/erdi/decisionOrgs")
    public List<String> getDecisionOrgs(){
        return contentViewRepository.getDistinctDecisionOrgs();
    }

    @GetMapping(path = "/erdi/infoTypeIds")
    public List<String> getInfoTypeIds(){
        return contentViewRepository.getDistinctInfoTypeIds();
    }

    @GetMapping(path = "/erdi/single")
    //@PreAuthorize("hasAnyRole('ROLE_OPERATOR')")
    public Optional<ContentView> getContentById(
            @RequestParam Long id) {
        return contentService.getFormalErdiView(id);
    }

    @GetMapping(path = "/check_erdi", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseStatusString checkErdi(@RequestParam(defaultValue = "") String url) {
        return infoService.searchCheckUnit(url);
    }

    @GetMapping(path = "/erdi/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM')")
    public ErdiStatus checkErdiStatus(@RequestParam Long id) {
        //Добавленные менее чем за сутки не нужны
        Date restrictionDate = DateUtils.addHours(new Date(), -24);
        return contentHistoryRepo.checkErdiStatus(id, restrictionDate);
    }

    @GetMapping(path = "/update_erdi")
    @PreAuthorize("hasAnyRole('ROLE_MANAGE_ERDI')")
    public ResponseEntity<String> update() {
        if (!erdiRestClient.getIsLoading()){
            CompletableFuture.runAsync(erdiRestClient::startUpdateErdi);
            return new ResponseEntity<>("Началась загрузка ЕРДИ", HttpStatus.OK);
        }
        return new ResponseEntity<>("Загрузка данных в процессе", HttpStatus.PROCESSING);
    }

    @GetMapping("/get_update_date")
    @PreAuthorize("hasAnyRole('ROLE_MANAGE_ERDI')")
    public String getUpdateDate() {
        return erdiRestClient.getUpdateDate();
    }

    @GetMapping("/state_update_erdi")
    @PreAuthorize("hasAnyRole('ROLE_MANAGE_ERDI')")
    public UpdateErdiState getState() {
        return new UpdateErdiState(
                erdiRestClient.getIsLoading(),
                erdiRestClient.getStateDetails(),
                erdiRestClient.getErrorMessage());
    }

    @GetMapping("/erdi/checkUnits")
    public ResponseEntity<List<CheckUnit>> getCheckUnits(@RequestParam("id") Long erdiId){
        List<CheckUnit> checkUnits = contentService.getActualCheckUnits(erdiId).stream()
            .map(contentCheckUnit -> new CheckUnit(contentCheckUnit.getContentId(), contentCheckUnit.getCheckUnitType(), contentCheckUnit.getCheckUnitValue()))
            .collect(Collectors.toList());
        if(checkUnits.size() > 0)
            return ResponseEntity.ok(checkUnits);
        else
            return ResponseEntity.badRequest().build();
    }

    @PostMapping("/erdi/checkUnits")
    public ResponseEntity<List<CheckUnit>> getCheckUnitsByIds(@RequestBody List<Long> erdiIds){
        List<CheckUnit> checkUnits = contentService.getActualCheckUnits(erdiIds).stream()
                .map(contentCheckUnit -> new CheckUnit(contentCheckUnit.getContentId(), contentCheckUnit.getCheckUnitType(), contentCheckUnit.getCheckUnitValue()))
                .collect(Collectors.toList());
        if(checkUnits.size() > 0)
            return ResponseEntity.ok(checkUnits);
        else
            return ResponseEntity.badRequest().build();
    }

    @PostMapping(path = "/erdi/check_units_count")
    public Long getCheckUnitsCount(@RequestBody List<Long> erdiIds) {
        return (long) contentCheckUnitRepository.findAllByErdIds(erdiIds).size();
    }
}
