package controllers;

import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.enums.ExecutionStatus;
import model.task.Arrangement;
import model.task.ExecutionStatusStatistics;
import model.task.FormalTask;
import model.traffic.Traffic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import repositories.ArrangementRepo;
import repositories.TrafficRepository;
import rest.ActRequest;
import rest.ArrangementActData;
import restapi.ppm.ArrangementUploader;
import services.arrangement.impl.ArrangementService;

import java.util.*;

/**
 * Creation date: 21.05.2019
 * Author: asavin
 */

@RestController
@RequestMapping(path = "/arrangements", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_MANAGE_ARRANGEMENT')")
@RequiredArgsConstructor(onConstructor_={@Autowired})
@Slf4j
public class ArrangementController {

    private final ArrangementRepo arrangementRepo;
    private final ArrangementService arrangementService;
    private final ArrangementUploader arrangementUploader;
    private final TrafficRepository trafficRepository;

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
    public List<ExecutionStatusStatistics> getSummary(){
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
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String fgisId,
            @RequestParam(required = false) String operator){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        return arrangementRepo.findPageFiltered(statuses, operator, fgisId, page);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    public Arrangement postArrangement(@RequestBody Arrangement arrangement, @RequestParam("formalTaskId") FormalTask formalTask){
        checkAndSetDeadlineDate(formalTask, arrangement);
        return arrangementService.saveArrangement(arrangement, formalTask);

    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Arrangement update(@RequestBody Arrangement newArrangement, @RequestParam("id") Arrangement arrangement) {
        if(arrangement == null) {
            throw new AS_15_8_PPT_Exception("Ошибка изменения мероприятия! Мероприятие не было найдено в БД");
        } else if (arrangement.getStatus() != ExecutionStatus.NEW) {
            throw new AS_15_8_PPT_Exception("Ошибка изменения мероприятия! Неверный статус: " + arrangement.getStatus().getDescription());
        }
        checkAndSetDeadlineDate(arrangement.getFormalTask(), newArrangement);
        return arrangementRepo.save(replaceFields(newArrangement, arrangement));
    }

    private void checkAndSetDeadlineDate(FormalTask formalTask, Arrangement arrangement){
        if(formalTask == null){
            throw new AS_15_8_PPT_Exception("Ошибка установки даты 'Выполнить до' мероприятию " + arrangement.getId() + ". Поручение не найдено в БД");
        }
        if(formalTask.getDeadlineDate() != null && (arrangement.getDeadlineDate() == null || arrangement.getDeadlineDate().isAfter(formalTask.getDeadlineDate()))){
            arrangement.setDeadlineDate(formalTask.getDeadlineDate());
        }
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
            //В ППМ отправляем только не запланированные мероприятия
            if(arrangement.getStatus().equals(ExecutionStatus.NEW) || arrangement.getStatus().equals(ExecutionStatus.FORMED)){
                if (arrangement.getIsManual()) {
                    arrangementUploader.sendManualArrangementToDispatcher(arrangement);
                } else {
                    arrangementUploader.updateArrangement(arrangement);
                }
            } else {
                throw new AS_15_8_PPT_Exception("Ошибка отправки мероприятия в ППМ. Мероприятие " + arrangement.getId() + " имеет недопустимый статус: " + arrangement.getStatus());
            }
        } else {
            throw new AS_15_8_PPT_Exception("Ошибка отправки мероприятия в ППМ. Мероприятие не было найдено в БД");
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_SYSTEM')")
    @GetMapping(path = "/confirm_success_sent", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public void confirmSuccessSent(Long arrangementId){
        log.info("Уведомление об успешной отправке мероприятия. ID мероприятия: {}", arrangementId);

        Optional<Arrangement> optionalArrangement = arrangementRepo.findById(arrangementId);
        Arrangement arrangement = optionalArrangement.orElse(null);
        if (arrangement == null){
            log.info("Arrangement с id = {} не найден!", arrangementId);
        }
        else if (arrangement.getStatus() == ExecutionStatus.FINISHED) {
            arrangementRepo.updateStatusById(arrangementId, ExecutionStatus.ACT_SENT);
            log.info("Состояние у Arrangement с id = {} изменено на : {}", arrangement.getId(), ExecutionStatus.ACT_SENT);
        }
        else {
            log.info("Состояние Arrangement с id = {} не изменилось", arrangementId);
        }
    }

    @GetMapping(path = "/ready_for_act")
    public Boolean readyForAct(@RequestParam Long id){
        Optional<Arrangement> optArrangement = arrangementRepo.findById(id);
        if (!optArrangement.isPresent())
            throw new AS_15_8_PPT_Exception("Arrangement не найден, id = " + id);

        Arrangement arrangement = optArrangement.get();
        FormalTask formalTask = arrangement.getFormalTask();

        Set<ExecutionStatus> states =
                new HashSet<>(Arrays.asList(ExecutionStatus.STOPPED,ExecutionStatus.FINISHED, ExecutionStatus.ACT_SENT));
        Boolean res =
                formalTask.getMissionId() != null && states.contains(arrangement.getStatus());
        return res;
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
        arrangement.setDeadlineDate(newArrangement.getDeadlineDate());
        arrangement.setTrafficId(newArrangement.getTrafficId());
        arrangement.setIsActAvailable(newArrangement.getIsActAvailable());
        arrangement.setInterruptViolationNumber(newArrangement.getInterruptViolationNumber());
        if (arrangement.getStatus().equals(ExecutionStatus.NEW))
            arrangement.setIsManual(newArrangement.getIsManual());
        Traffic traffic = trafficRepository.findById(newArrangement.getTrafficId())
                .orElseThrow(() -> new AS_15_8_PPT_Exception("Ошибка при добавлении трафика! Трафик не найден по id: " + newArrangement.getTrafficId()));
        arrangement.setTrafficName(traffic.getName());
        return arrangement;
    }

    @PreAuthorize("hasAnyRole('ROLE_SYSTEM', 'ROLE_MANAGE_ARRANGEMENT')")
    @GetMapping("/execution_status_description")
    public String getExecutionStatusName(@RequestParam("id") Arrangement arrangement) {
        return arrangement.getStatus().getDescription();
    }
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM', 'ROLE_MANAGE_ARRANGEMENT')")
    @GetMapping("/execution_status")
    public String getExecutionStatus(@RequestParam("id") Arrangement arrangement) {
        return arrangement.getStatus().name();
    }

    @PreAuthorize("hasAnyRole('ROLE_SYSTEM', 'ROLE_MANAGE_ARRANGEMENT')" )
    @GetMapping(path = "/act_available_for_automatic_send")
    public Optional<Boolean> isActAvailable(@RequestParam("id") Optional<Arrangement> arrangement){
        arrangement.orElseThrow(()-> new AS_15_8_PPT_Exception("Arrangement не найден"));
        return Optional.ofNullable(arrangement.get().getIsActAvailable());
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM')" )
    @GetMapping(path = "/act_sent_status")
    public void changeActStatus(@RequestParam("id") Optional<Arrangement> arrangement){
        arrangement.orElseThrow(()-> new AS_15_8_PPT_Exception("Arrangement не найден"));
        arrangement.get().setStatus(ExecutionStatus.ACT_SENT);
        arrangementRepo.save(arrangement.get());
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM')" )
    @GetMapping(path = "/interrupt_violation_number")
    public Long changeActStatus(@RequestParam("id") Arrangement arrangement){
        Optional.ofNullable(arrangement).orElseThrow(()-> new AS_15_8_PPT_Exception("Arrangement не найден"));
        return arrangement.getInterruptViolationNumber();
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM')" )
    @GetMapping(path = "/access_tool")
    public String getAccessTool(@RequestParam("id") Arrangement arrangement){
        Optional.ofNullable(arrangement).orElseThrow(()-> new AS_15_8_PPT_Exception("Arrangement не найден"));
        return arrangement.getAccessTool();
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    @PutMapping(path = "/info_about_act")
    public ResponseEntity updateArrangementAboutAct(@RequestBody ActRequest actRequest) {
        if(actRequest != null) {

            Arrangement arr = arrangementRepo.findById(actRequest.getArragementId()).orElseThrow(() ->
                    new AS_15_8_PPT_Exception("Невозможно обновить иформацию о акте для мероприятия, мероприятие не найдено id:"  + actRequest.getArragementId()));
            String operator;

            if (actRequest.isGeneratedAutomatically()) {
                 operator = arr.getFormalTask().getOperator();
            } else {
                operator = actRequest.getOperatorName();
            }



            arr.setActCreationOperator(operator);
            arrangementRepo.save(arr);
            return ResponseEntity.ok(arr);
        } return ResponseEntity.badRequest().build();
    }
}
