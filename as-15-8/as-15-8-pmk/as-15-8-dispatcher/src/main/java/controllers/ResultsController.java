package controllers;

import controllers.helpers.SortingHelper;
import enums.CheckUnitJobResult;
import enums.SortingDirection;
import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.enums.UserResult;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.*;

import java.util.List;
import java.util.Map;

/**
 * Creation date: 29.05.2019
 * Author: asavin
 * Выдача результатов проведения мероприятия на фронт
 */

@RestController
@RequestMapping(path = "/results", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ResultsController {

    private final ResultRepo resultRepo;
    private final ResultScreenShotRepo resultScreenShotRepo;
    private final NmapDetailResultRepo nmapDetailResultRepo;

    @PreAuthorize("hasRole('ROLE_OPERATOR')")
    @GetMapping
    public Page<Result> findList(
            @RequestParam Long arrangementId,
            @RequestParam(required = false) List<CheckUnitJobResult> checkUnitJobResults,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        if (checkUnitJobResults != null) {
            return resultRepo.findByArrangementIdAndResultIn(arrangementId, checkUnitJobResults, page);
        } else {
            return Strings.isEmpty(query) ?
                    resultRepo.findAllByArrangementId(arrangementId, page) :
                    resultRepo.findAllByArrangementAndQuery(arrangementId, query, page);
        }
    }

    @PreAuthorize("hasRole('ROLE_OPERATOR')")
    @GetMapping("/completion")
    public int getCompletionPercent(@RequestParam Long arrangementId){
        return resultRepo.getCompletionPercent(arrangementId);
    }

    @GetMapping(path = "/screenshot", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    ResponseEntity<byte[]> getScreenshot(@RequestParam Long id){
        return resultScreenShotRepo.findById(id)
                .map(resultScreenShot -> ResponseEntity.ok(resultScreenShot.getScreenshot()))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping(path = "/etalon_screenshot", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    ResponseEntity<byte[]> getEtalonScreenshot(@RequestParam Long id){
        return resultScreenShotRepo.findById(id)
                .map(resultScreenShot -> ResponseEntity.ok(resultScreenShot.getEtalonScreenshot()))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping(path = "/nmap_log", produces = MediaType.TEXT_PLAIN_VALUE)
    public @ResponseBody ResponseEntity<String> getNmapLog(@RequestParam Long id){
        return nmapDetailResultRepo.findById(id)
                .map(nmapDetailResult -> ResponseEntity.ok(nmapDetailResult.getLog()))
                .orElse(ResponseEntity.noContent().build());
    }

    @PreAuthorize("hasRole('ROLE_OPERATOR')")
    @PostMapping(path = "/user_result", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postUserResult(@RequestParam Long id, @RequestBody Map<String, String> userResult) {
    	return resultRepo.findById(id)
    		.map(arrResult -> {
    			UserResult result;
    			try {
    				result = UserResult.valueOf(userResult.get("userResult"));
    			} catch (Exception ex) {
                    throw AS_15_8_DispatcherException.logAndGet(log, "Ошибка сохранения результатов от пользователя: " + ex.getMessage()); }
    			arrResult.setUserResult(result);
    			arrResult.setUserDescription(userResult.get("userDescription"));
                resultRepo.save(arrResult);
    			return new ResponseEntity<Object>(arrResult, HttpStatus.OK);
    		}).orElseGet(() -> {
    			String errorMessage = "Ошибка! Результат не найден по идентификатору: "+id;
				log.error("Ошибка при сохранении результатов от пользователя", new AS_15_8_DispatcherException(errorMessage));
				return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    		});
    }
    
}
