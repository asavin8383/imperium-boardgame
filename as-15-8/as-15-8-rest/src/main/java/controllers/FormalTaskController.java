package controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import controllers.helpers.UserHelper;
import model.task.FormalTask;
import repositories.FormalTaskRepository;

@RestController
@RequestMapping(path="/formal_tasks", produces=MediaType.APPLICATION_JSON_VALUE)
public class FormalTaskController {

	@Autowired
	private UserHelper userHelper; 
	
	@Autowired
	private FormalTaskRepository formalTaskRepo;
	
	@PostMapping(consumes=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public FormalTask postFormalTask(@RequestBody FormalTask formalTask, Authentication authentication) {
		formalTask.setAuthor(userHelper.getOrCreateUser(authentication));
		return formalTaskRepo.save(formalTask);
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
}
