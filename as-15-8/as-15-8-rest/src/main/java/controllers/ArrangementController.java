package controllers;

import controllers.helpers.ArrangementExecutionHelper;
import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_Exception;
import lombok.extern.slf4j.Slf4j;
import model.enums.ExecutionStatus;
import model.task.Arrangement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementRepository;
import repositories.FormalTaskRepository;
import services.arrangement.ArrangementStatusService;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Creation date: 21.05.2019
 * Author: asavin
 */

@RestController
@RequestMapping(path = "/arrangements", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@Slf4j
public class ArrangementController {

    private ArrangementRepository arrangementRepo;
    private FormalTaskRepository formalTaskRepo;
    private ArrangementExecutionHelper arrangementExecutionHelper;
    private ArrangementStatusService arrangementStatusService;

    @Autowired
    public ArrangementController(ArrangementRepository arrangementRepo,
                                 FormalTaskRepository formalTaskRepo,
                                 ArrangementExecutionHelper arrangementExecutionHelper,
                                 ArrangementStatusService arrangementStatusService
                                 ) {
        this.arrangementRepo = arrangementRepo;
        this.formalTaskRepo = formalTaskRepo;
        this.arrangementExecutionHelper = arrangementExecutionHelper;
        this.arrangementStatusService = arrangementStatusService;
    }

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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    public Arrangement postArrangement(@RequestBody Arrangement arrangement, @RequestParam Long formalTaskId){
        return formalTaskRepo.findById(formalTaskId)
            .map(formalTask -> {
                arrangement.setFormalTask(formalTask);
                return arrangementRepo.save(arrangement);
            })
            .orElseThrow(() -> new AS_15_8_Exception("Error creating arrangement! Formal task was not found by id: " + formalTaskId));

    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Arrangement replaceFormalTask(@RequestBody Arrangement newArrangement, @RequestParam Long id) {
        return arrangementRepo.findById(id)
                .map(arrangement -> arrangementRepo.save(replaceFields(newArrangement, arrangement)))
                .orElseGet(() -> {
                    newArrangement.setId(id);
                    return arrangementRepo.save(newArrangement);
                });
    }

    @PostMapping(path = "/plan")
    public ResponseEntity<Arrangement> planArrangement(@RequestParam Long id){
        return arrangementRepo.findById(id)
                .map(arrangement -> {
                    //Проверим, не нужно ли сменить статус(сначала у мероприятия, а потом и у задания)
                    if(isStatusChanged(arrangement)){
                        arrangementStatusService.processArrangementStatusChange(arrangement);
                    }
                    return new ResponseEntity<>(arrangement, HttpStatus.OK);
                }).orElseGet(()-> new ResponseEntity<>(null, HttpStatus.NO_CONTENT));
    }

    @DeleteMapping
    public Long deleteArrangement(@RequestParam Long id) {
        return arrangementRepo.findById(id)
                .map(arrangement -> {
                    arrangementRepo.delete(arrangement);
                    return arrangement.getId();
                })
                .orElseThrow(() -> new AS_15_8_Exception("Error deleting arrangement! Arrangement was not found by id: " + id));
    }

    /**
     * Запуск мероприятия на выполнение
     * Необходимым условием запуска является статус "PLANNED"
     * @param id идентификатор мероприятия
     * @return запущенное мероприятие
     */
    @GetMapping(path = "/run")
    public ResponseEntity<Arrangement> runArrangement(@RequestParam Long id){
        return arrangementRepo.findById(id)
            .map(arrangement -> {
                if(arrangement.getStatus().equals(ExecutionStatus.PLANNED)||arrangement.getStatus().equals(ExecutionStatus.ACTION_REQUIRED)) {
                    arrangementExecutionHelper.sendJobToDispatcher(arrangement);
                    //Устанавливаем дату запуска(актуально только для впервые запущенных мероприятий)
                    if(arrangement.getStatus().equals(ExecutionStatus.PLANNED)){
                        arrangement.setStartDate(LocalDateTime.now());
                    }
                    arrangement.setStatus(ExecutionStatus.RUNNING);
                    arrangementStatusService.processArrangementStatusChange(arrangement);
                    arrangementRepo.save(arrangement);
                    return new ResponseEntity<>(arrangement, HttpStatus.OK);
                }
                return new ResponseEntity<>(arrangement, HttpStatus.NOT_ACCEPTABLE);
            })
            .orElseThrow(() -> new AS_15_8_Exception("Error running arrangement! Arrangement was not found by id: " + id));
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
        arrangement.setPlannedDate(newArrangement.getPlannedDate());
        return arrangement;
    }

    /**
     * Проверяет состояние мероприятия и меняет ему статус при выполнении условий смены
     * @param arrangement мероприятие
     * @return Изменился ли статус
     */
    private boolean isStatusChanged(Arrangement arrangement){
        //Если новому мероприятию запланировали дату запуска в будущем,
        // при этом не пустой список ЕРДИ,
        // оно становится PLANNED
        if(arrangement.getStatus().equals(ExecutionStatus.NEW) &&
                arrangement.getPlannedDate() != null &&
                arrangement.getArrangementItems()!=null &&
                arrangement.getArrangementItems().size() > 0){
            arrangement.setStatus(ExecutionStatus.PLANNED);
            log.info("Arrangement status changed to " + arrangement.getStatus());
            return true;
        }
        log.warn("Arrangement status was not changed. Planned date: " + arrangement.getPlannedDate() +
                ", arrangement items: " + arrangement.getArrangementItems().stream()
                .map(arrangementItem -> arrangementItem.getId().toString()).collect(Collectors.joining("'")));
        return false;
    }




}
