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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import repositories.FormalTaskRepository;
import rest.MissionData;
import services.ClientNotificationService;
import users.User;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path="/formal_tasks", produces=MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_FORMAL_TASK')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class FormalTaskController {

	private final FormalTaskRepository formalTaskRepo;
	private final ClientNotificationService clientNotificationService;
	private final OAuth2RestTemplate oAuth2RestTemplate;

	@Value("${gateway.url}")
	private String gatewayUrl;

	private final static String SOIB_URI = "/security/user/operator";

	@PostMapping(consumes=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public FormalTask postFormalTask(@RequestBody FormalTask formalTask, Principal principal) {
		formalTask.setAuthor(principal.getName());
		formalTask.setStatus(ExecutionStatus.NEW);
		formalTask.setCreationDate(LocalDateTime.now());
		return formalTaskRepo.save(formalTask);
	}

	@GetMapping
	public Page<FormalTask> findList(
			@RequestParam(required = false) Long taskId,
			@RequestParam(required = false) String operator,
			@RequestParam(required = false) String fgisId,
			@RequestParam(required = false) SortingDirection sortingDirection,
			@RequestParam(required = false) String sortingColumn,
			@RequestParam(defaultValue = "0") int pageNumber,
			@RequestParam(defaultValue = "10") int pageSize){
		PageRequest page = PageRequest.of(
				pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
		return formalTaskRepo.findPage(taskId, operator, fgisId, page);
	}

	@GetMapping("{task}")
	public FormalTask findById(@PathVariable FormalTask task){
		return task;
	}

	@GetMapping("{task}/automatic_act_send")
	public Boolean isAutomaticActSendAvailable(@PathVariable FormalTask task){
		if (task == null)
			throw new AS_15_8_PPT_Exception("Formal task not found: " + task);
		if (task.getMissionId() != null)
			return true;
		else return false;
	}

	@PutMapping
	public FormalTask replaceFormalTask(@RequestBody FormalTask newFormalTask, @RequestParam("id") FormalTask existingTask){
		if (existingTask == null) {
			return formalTaskRepo.save(newFormalTask);
		} else {
			return formalTaskRepo.save(replaceFields(newFormalTask, existingTask));
		}
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

	@PreAuthorize("hasRole('ROLE_SYSTEM')")
	@PostMapping(path = "/create_with_mission")
	public void postFormalTask(@RequestBody MissionData missionData, Principal principal) {
		log.info("Запрос создания FormalTask по поручению: {}", missionData);
		if (missionData.getId() == null || missionData.getName() == null || missionData.getOrigId() == null)
			throw new AS_15_8_PPT_Exception("Не заданы все требуемые поля: id, name, origId");

		createFormalTaskByMission(missionData, principal.getName());
		//Сохранение уведомления в БД
		getOperatorsFromAuthServer().forEach(operator ->
				clientNotificationService.saveNotification(operator,
			"Получено новое поручение из ППП РА: " + missionData.getName()
				)
		);

	}

	private FormalTask replaceFields(FormalTask newTask, FormalTask storedTask){
		storedTask.setTitle(newTask.getTitle());
		storedTask.setModificationDate(LocalDateTime.now());
		storedTask.setDeadlineDate(newTask.getDeadlineDate());
		storedTask.setAgreed(newTask.isAgreed());
		storedTask.setAuthor(newTask.getAuthor());
		storedTask.setPriority(newTask.getPriority());
		storedTask.setOperator(newTask.getOperator());
		return storedTask;
	}

	private void createFormalTaskByMission(MissionData missionData, String operator){
		Long cnt = formalTaskRepo.countByMissionId(missionData.getId());
		if (cnt > 0) {
			log.info("FormalTask не создан. Причина: missionId = {} уже существует", missionData.getId());
			return;
		}

		FormalTask formalTask = new FormalTask();
		formalTask.setTitle(missionData.getName());
		formalTask.setMissionId(missionData.getId());
		formalTask.setCreationDate(LocalDateTime.now());
		formalTask.setAgreed(true);
		formalTask.setStatus(ExecutionStatus.NEW);
		formalTask.setOperator(operator);
		formalTask.setFgisId(missionData.getOrigId());
		FormalTask res = formalTaskRepo.save(formalTask);

		log.info("Создан FormalTask: {}", res);
	}

	private List<String> getOperatorsFromAuthServer(){
		log.info("Отправка запроса на получение списка операторов сервису аутентификации");
		try {
			List<String> operators = new ArrayList<>();
			User[] queryResult = oAuth2RestTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(SOIB_URI).build().toString(), User[].class);
			if(queryResult != null){
				operators = Arrays.asList(queryResult).stream().map(user -> user.getLogin()).collect(Collectors.toList());
			}
			log.info("Получено {} операторов", operators.size());
			return operators;
		} catch (HttpClientErrorException | HttpServerErrorException ex) {
			throw AS_15_8_PPT_Exception.logAndGet(log, String.format("Ошибка отправки запроса на получение списка операторов сервису аутентификации, код возврата %s", ex.getStatusCode()));
		}
	}

	@GetMapping("/get_by_orig_id/{fgis_id}")
	public FormalTask getByFgisId(@PathVariable String fgis_id){
		return formalTaskRepo.findByFgisId(fgis_id);
	}

}
