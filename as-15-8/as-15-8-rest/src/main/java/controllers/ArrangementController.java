package controllers;

import controllers.helpers.ArrangementExecutionHelper;
import exceptions.AS_15_8_Exception;
import model.enums.ExecutionStatus;
import model.task.Arrangement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementRepository;
import repositories.ArrangementRepositoryAdvanced;
import repositories.FormalTaskRepository;

import java.time.LocalDateTime;

/**
 * Creation date: 21.05.2019
 * Author: asavin
 */

@RestController
@RequestMapping(path = "/arrangements", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
public class ArrangementController {

    private ArrangementRepository arrangementRepo;
    private FormalTaskRepository formalTaskRepo;
    private ArrangementRepositoryAdvanced arrangementRepoAdvanced;
    private ArrangementExecutionHelper arrangementExecutionHelper;

    @Autowired
    public ArrangementController(ArrangementRepository arrangementRepo,
                                 FormalTaskRepository formalTaskRepo,
                                 ArrangementRepositoryAdvanced arrangementRepoAdvanced,
                                 ArrangementExecutionHelper arrangementExecutionHelper
                                 ) {
        this.arrangementRepo = arrangementRepo;
        this.formalTaskRepo = formalTaskRepo;
        this.arrangementRepoAdvanced = arrangementRepoAdvanced;
        this.arrangementExecutionHelper = arrangementExecutionHelper;
    }

    @GetMapping
    public Page<Arrangement> findList(
            @RequestParam(required = false) Long formalTaskId,
            @RequestParam(required = false) Long id,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, Sort.by("id").ascending());
        return arrangementRepoAdvanced.findPage(formalTaskId, id, page);
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
                    //Проверим, не нужно ли сменить статус
                    arrangementExecutionHelper.checkArrangementStatus(arrangement);
                    arrangementRepo.save(arrangement);
                    return new ResponseEntity<>(arrangement, HttpStatus.OK);
                }).orElseGet(()-> new ResponseEntity<>(null, HttpStatus.NO_CONTENT));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
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
                if(arrangement.getStatus().equals(ExecutionStatus.PLANNED)) {
                    arrangementExecutionHelper.sendJobToDispatcher(arrangement);
                    arrangement.setStartDate(LocalDateTime.now());
                    arrangement.setStatus(ExecutionStatus.RUNNING);
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
        arrangement.setStartDate(newArrangement.getStartDate());
        arrangement.setEndDate(newArrangement.getEndDate());
        arrangement.setTitle(newArrangement.getTitle());
        return arrangement;
    }




}
