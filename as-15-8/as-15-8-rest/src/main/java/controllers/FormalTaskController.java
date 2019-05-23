package controllers;

import exceptions.AS_15_8_Exception;
import model.task.FormalTask;
import model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import repositories.FormalTaskRepository;
import repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@RestController
@RequestMapping(path="/formal_tasks", produces=MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_OPERATOR')")
public class FormalTaskController {

	private FormalTaskRepository formalTaskRepo;
	private UserRepository userRepo;

	@Autowired
	public FormalTaskController(FormalTaskRepository formalTaskRepo, UserRepository userRepo) {
		this.formalTaskRepo = formalTaskRepo;
		this.userRepo = userRepo;
	}

	@PostMapping(consumes=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public CompletionStage<FormalTask> postFormalTask(@RequestBody FormalTask formalTask, Authentication auth) {
		String userName = ((User)auth.getPrincipal()).getUserName();
		return CompletableFuture.supplyAsync(() -> userRepo.findByUserName(userName))
			.thenApply(curUser -> curUser
				.map(user -> {
					formalTask.setUser(user);
					return formalTaskRepo.save(formalTask);
				})
				.orElseThrow(() -> {return new AS_15_8_Exception("User was not found by username: " + userName);
				})
			);
	}
	
	@GetMapping
	public Page<FormalTask> findList(
			@RequestParam(required = false) Long taskId,
			@RequestParam(required = false) Long userId,
			@RequestParam(defaultValue = "0") int pageNumber,
			@RequestParam(defaultValue = "10") int pageSize){
		PageRequest page = PageRequest.of(
				pageNumber, pageSize, Sort.by("creationDate").descending());
		return formalTaskRepo.findPage(taskId, userId, page);
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

	private FormalTask replaceFields(FormalTask newTask, FormalTask storedTask){
		storedTask.setTitle(newTask.getTitle());
		storedTask.setModificationDate(LocalDateTime.now());
		storedTask.setAgreed(newTask.isAgreed());
		storedTask.setAuthor(newTask.getAuthor());
		storedTask.setDeadlineDate(newTask.getDeadlineDate());
		storedTask.setFgisId(newTask.getFgisId());
		storedTask.setPriority(newTask.getPriority());
		storedTask.setStatus(newTask.getStatus());
		storedTask.setUser(newTask.getUser());
		return storedTask;
	}
}
