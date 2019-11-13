package controllers;

import controllers.helpers.SortingHelper;
import enums.ExecutionStatus;
import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.task.ExecutionStatusStatistics;
import model.task.FormalTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.FormalTaskRepository;
import rest.MissionData;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@RestController
@RequestMapping(path="/formal_tasks", produces=MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class FormalTaskController {

	private final FormalTaskRepository formalTaskRepo;

	@PostMapping(consumes=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public FormalTask postFormalTask(@RequestBody FormalTask formalTask, Principal principal) {
		formalTask.setAuthor(principal.getName());
		formalTask.setStatus(ExecutionStatus.NEW);
		return formalTaskRepo.save(formalTask);
	}
	
	@GetMapping
	public Page<FormalTask> findList(
			@RequestParam(required = false) Long taskId,
			@RequestParam(required = false) String operator,
			@RequestParam(required = false) SortingDirection sortingDirection,
			@RequestParam(required = false) String sortingColumn,
			@RequestParam(defaultValue = "0") int pageNumber,
			@RequestParam(defaultValue = "10") int pageSize){
		PageRequest page = PageRequest.of(
				pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
		return formalTaskRepo.findPage(taskId, operator, page);
	}

	@PutMapping
	public CompletionStage<FormalTask> replaceFormalTask(@RequestBody FormalTask newFormalTask, @RequestParam Long id){
		return CompletableFuture.supplyAsync(() -> formalTaskRepo.findById(id))
			.thenApply(curFormalTask -> curFormalTask
				.map(formalTask -> formalTaskRepo.save(replaceFields(newFormalTask, formalTask)))
				.orElseGet(() -> {
					newFormalTask.setId(id);
					return formalTaskRepo.save(newFormalTask);
				})
			);
	}

	@GetMapping(path = "/summary")
	public List<ExecutionStatusStatistics> groupByStatusList(){
		return formalTaskRepo.findSummaryByStatus();
	}

	@GetMapping(path = "/status")
	public Page<FormalTask> findListByStatus(
			@RequestParam ExecutionStatus status,
			@RequestParam(required = false) SortingDirection sortingDirection,
			@RequestParam(required = false) String sortingColumn,
			@RequestParam(defaultValue = "0") int pageNumber,
			@RequestParam(defaultValue = "10") int pageSize){
		PageRequest page = PageRequest.of(
				pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
		return formalTaskRepo.findAllByStatus(status, page);
	}

	@GetMapping(path = "/statuses")
	public Page<FormalTask> findListByStatuses(
			@RequestParam List<ExecutionStatus> statuses,
			@RequestParam(required = false) SortingDirection sortingDirection,
			@RequestParam(required = false) String sortingColumn,
			@RequestParam(defaultValue = "0") int pageNumber,
			@RequestParam(defaultValue = "10") int pageSize){
		PageRequest page = PageRequest.of(
				pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
		return formalTaskRepo.findAllByStatusIn(statuses, page);
	}

	@DeleteMapping
	public Long deleteFormalTask(@RequestParam Long id) {
		return formalTaskRepo.findById(id)
				.map(formalTask -> {
					formalTaskRepo.delete(formalTask);
					return formalTask.getId();
				})
				.orElseThrow(() -> new AS_15_8_PPT_Exception("Error deleting formal task! Formal task was not found by id: " + id));
	}

	@PreAuthorize("hasAnyRole('ROLE_SYSTEM')")
	@PostMapping(path = "/create_with_mission")
	public void postFormalTask(@RequestBody MissionData missionData, Principal principal) {
		log.info("Запрос создания FormalTask по поручению: {}", missionData);
		if (missionData.getId() == null || missionData.getName() == null)
			throw new AS_15_8_PPT_Exception("Не заданы все требуемые поля: id, name");

		createFormalTaskByMission(missionData, principal.getName());
	}

	private FormalTask replaceFields(FormalTask newTask, FormalTask storedTask){
		storedTask.setTitle(newTask.getTitle());
		storedTask.setModificationDate(LocalDateTime.now());
		storedTask.setAgreed(newTask.isAgreed());
		storedTask.setAuthor(newTask.getAuthor());
		storedTask.setDeadlineDate(newTask.getDeadlineDate());
		storedTask.setPriority(newTask.getPriority());
		storedTask.setOperator(newTask.getOperator());
		return storedTask;
	}

	private FormalTask createFormalTaskByMission(MissionData missionData, String operator){
		Long cnt = formalTaskRepo.countByMissionId(missionData.getId());
		if (cnt > 0) {
			log.info("FormalTask не создан. Причина: missionId = {} уже существует", missionData.getId());
			return null;
		}

		FormalTask formalTask = new FormalTask();
		formalTask.setTitle(missionData.getName());
		formalTask.setMissionId(missionData.getId());
		formalTask.setCreationDate(LocalDateTime.now());
		formalTask.setAgreed(true);
		formalTask.setStatus(ExecutionStatus.NEW);
		formalTask.setOperator(operator);
		FormalTask res = formalTaskRepo.save(formalTask);

		log.info("Создан FormalTask: {}", res);
		return res;
	}

}
