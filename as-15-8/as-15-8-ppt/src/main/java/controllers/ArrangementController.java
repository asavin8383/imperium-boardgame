package controllers;

import controllers.helpers.SortingHelper;
import enums.ExecutionStatus;
import enums.SortingDirection;
import events.producers.rest.ppm.ArrangementUploader;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.task.Arrangement;
import model.task.ArrangementStatistics;
import model.task.FormalTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import repositories.ArrangementRepo;
import rest.ArrangementActData;
import services.arrangement.impl.ArrangementService;

import java.util.List;
import java.util.Optional;

/**
 * Creation date: 21.05.2019
 * Author: asavin
 */

@RestController
@RequestMapping(path = "/arrangements", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_={@Autowired})
@Slf4j
public class ArrangementController {

    private final ArrangementRepo arrangementRepo;
    private final ArrangementService arrangementService;
    private final ArrangementUploader arrangementUploader;

    @GetMapping
    public Page<Arrangement> findList(
            @RequestParam(required = false) Long formalTaskId,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        return arrangementRepo.findPage(formalTaskId, id, page);
    }

    @GetMapping("{id}")
    public Arrangement findById(@PathVariable Long id){
        Optional<Arrangement> byId = arrangementRepo.findById(id);
        return byId.orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "Arrangement not found " + id));
    }

    @GetMapping(path="/summary")
    public List<ArrangementStatistics> getSummary(){
        return arrangementRepo.findSummaryByStatus();
    }

    @GetMapping(path = "/status")
    public Page<Arrangement> findListByStatus(
            @RequestParam ExecutionStatus status,
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        return arrangementRepo.findAllByStatus(status, page);
    }

    @GetMapping(path = "/statuses")
    public Page<Arrangement> findListByStatuses(
            @RequestParam List<ExecutionStatus> statuses,
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        return arrangementRepo.findAllByStatusIn(statuses, page);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    public Arrangement postArrangement(@RequestBody Arrangement arrangement, @RequestParam("formalTaskId") FormalTask formalTask){
        return arrangementService.saveArrangement(arrangement, formalTask);

    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Arrangement replaceFormalTask(@RequestBody Arrangement newArrangement, @RequestParam("id") Arrangement arrangement) {
        return arrangementRepo.save(replaceFields(newArrangement, arrangement));
    }

    @DeleteMapping
    public Long deleteArrangement(@RequestParam Long id) {
        return arrangementRepo.findById(id)
                .map(arrangement -> {
                    arrangementRepo.delete(arrangement);
                    return arrangement.getId();
                })
                .orElseThrow(() -> new AS_15_8_PPT_Exception("Error deleting arrangement! Arrangement was not found by id: " + id));
    }


    @GetMapping(path = "/upload")
    public void uploadArrangement(@RequestParam("id") Arrangement arrangement){
        if(arrangement!= null) {
            arrangementUploader.updateArrangement(arrangement);
        } else {
            throw AS_15_8_PPT_Exception.logAndGet(log, "Ошибка отправки мероприятия в ППМ. Мероприятие не было найдено в БД");
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_SYSTEM')")
    @GetMapping("/act_data")
    public ArrangementActData accessToolByArrangementId(Long arrangement_id) {
        return arrangementRepo.findArrangementActData(arrangement_id);
    }

    /**
     * Заменяет доступные поля мероприятия из БД полученными с фронта
     * @param newArrangement полученное с фронта мероприятие
     * @param arrangement мероприятие, сохраненное в БД
     * @return изменнённое мероприятие
     */
    private Arrangement replaceFields(Arrangement newArrangement, Arrangement arrangement) {
        arrangement.setAccessTool(newArrangement.getAccessTool());
        arrangement.setTitle(newArrangement.getTitle());
        arrangement.setPlannedStartTime(newArrangement.getPlannedStartTime());
        arrangement.setPlannedEndTime(newArrangement.getPlannedEndTime());
        arrangement.setTraffic(newArrangement.getTraffic());
        return arrangement;
    }

}
