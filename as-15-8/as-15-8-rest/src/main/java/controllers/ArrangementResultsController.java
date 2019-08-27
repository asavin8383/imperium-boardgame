package controllers;

import checkUnits.CheckUnitType;
import controllers.helpers.SortingHelper;
import enums.CheckUnitJobResult;
import enums.SortingDirection;
import exceptions.AS_15_8_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.enums.UserResult;
import model.result.ArrangementResult;
import model.result.DetailedArrangementResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementResultRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
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
public class ArrangementResultsController {

    private final ArrangementResultRepository arrangementResultRepo;
    private final JdbcTemplate jdbcTemplate;

    @PreAuthorize("hasAnyRole('ROLE_OPERATOR', 'ROLE_ADMIN')")
    @GetMapping
    public Page<ArrangementResult> findList(
            @RequestParam Long arrangementId,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String checkUnitValue,
            @RequestParam(required = false) CheckUnitType checkUnitType,
            @RequestParam(required = false) List<CheckUnitJobResult> checkUnitJobResults,
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        return arrangementResultRepo.findPage(id, arrangementId, checkUnitValue, page, checkUnitType, checkUnitJobResults);
    }

    @GetMapping(path = "/screenshot", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    ResponseEntity<byte[]> getScreenshot(@RequestParam Long id){
        return receiveScreenshotFromDB(id, false);
    }

    @GetMapping(path = "/etalon_screenshot", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    ResponseEntity<byte[]> getEtalonScreenshot(@RequestParam Long id){
        return receiveScreenshotFromDB(id, true);
    }

    @PreAuthorize("hasAnyRole('ROLE_OPERATOR', 'ROLE_ADMIN')")
    @GetMapping(path = "/details")
    public DetailedArrangementResult getDetails(@RequestParam("id") DetailedArrangementResult detailedArrangementResult){
        return detailedArrangementResult;
    }

    @PreAuthorize("hasAnyRole('ROLE_OPERATOR', 'ROLE_ADMIN')")
    @PostMapping(path = "/user_result", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postUserResult(@RequestParam Long id, @RequestBody Map<String, String> userResult) {
    	return arrangementResultRepo.findById(id)
    		.map(arrResult -> {
    			UserResult result = null;
    			try {
    				result = UserResult.valueOf(userResult.get("userResult"));
    			} catch (Exception ex) {AS_15_8_Exception.logAndThrow(log, "Ошибка сохранения результатов от пользователя: " + ex.getMessage()); }
    			if(result == null) {
    				String errorMessage = "Ошибка! Результат " + userResult.get("userResult")+" не поддерживается!";
    				log.error("Ошибка при сохранении результатов от пользователя", new AS_15_8_Exception(errorMessage));
    				return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    			}
    			arrResult.setUserResult(result);
    			arrResult.setUserDescription(userResult.get("userDescription"));
    			arrangementResultRepo.save(arrResult);
    			return new ResponseEntity<>(arrResult, HttpStatus.OK);
    		}).orElseGet(() -> {
    			String errorMessage = "Ошибка! Результат не найден по идентификатору: "+id;
				log.error("Ошибка при сохранении результатов от пользователя", new AS_15_8_Exception(errorMessage));
				return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    		});
    }
    
    private ResponseEntity<byte[]> receiveScreenshotFromDB(Long id, boolean isEtalon){

        String fieldName = "screenshot";
        if (isEtalon) {
            fieldName = "etalon_screenshot";
        }

        String sql = "select " + fieldName + " from portal.arrangement_results where id = ?";

        byte[] result = jdbcTemplate.queryForObject(sql, new Object[]{id}, this::mapScreenShot);
        if (result != null) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    private byte[] mapScreenShot(ResultSet rs, int rowNum) throws SQLException {
        LobHandler lobHandler = new DefaultLobHandler();
        return lobHandler.getBlobAsBytes(rs,1);
    }
}
