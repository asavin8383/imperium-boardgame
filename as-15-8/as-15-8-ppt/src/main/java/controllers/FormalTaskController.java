package controllers;

import controllers.helpers.SortingHelper;
import enums.ExecutionStatus;
import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.task.ArrangementStatistics;
import model.task.FormalTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.FormalTaskRepository;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
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
		formalTask.setOperator(principal.getName());
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

	@GetMapping(path = "/group")
	public List<?> groupByStatusList(){
		return formalTaskRepo.getFormalTasksGroupingByStatus();
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

	@GetMapping(path="/summary")
	public List<ArrangementStatistics> getSummary(){
		return formalTaskRepo.findSummaryByStatus();
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

	private FormalTask replaceFields(FormalTask newTask, FormalTask storedTask){
		storedTask.setTitle(newTask.getTitle());
		storedTask.setModificationDate(LocalDateTime.now());
		storedTask.setAgreed(newTask.isAgreed());
		storedTask.setAuthor(newTask.getAuthor());
		storedTask.setDeadlineDate(newTask.getDeadlineDate());
		storedTask.setFgisId(newTask.getFgisId());
		storedTask.setPriority(newTask.getPriority());
		storedTask.setOperator(newTask.getOperator());
		return storedTask;
	}

}
