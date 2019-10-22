package controllers;

import controllers.enums.UploadingState;
import controllers.utils.SortingDirection;
import controllers.utils.SortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.scheme.PasdRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import repositories.PasdRepository;
import restapi.PASDRestClient;
import utils.Utils;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(path = "/pasd", produces = MediaType.APPLICATION_JSON_VALUE)
//@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class PasdController {

    private final PasdRepository pasdRepository;
    private final PASDRestClient pasdRestClient;
    private UploadingState state = UploadingState.ACTIVE;

    @GetMapping
    public ResponseEntity<Page<PasdRecord>> getPasdPage(@RequestParam(required = false) SortingDirection sortingDirection,
                                        @RequestParam(required = false) String sortingColumn,
                                        @RequestParam(defaultValue = "0") int pageNumber,
                                        @RequestParam(defaultValue = "10") int pageSize,
                                        @RequestParam(required = false) String query) {
        if (state != UploadingState.UPLOADING) {
            Pageable page = PageRequest.of(pageNumber, pageSize,
                    SortingHelper.createSorting(sortingDirection, sortingColumn));
            Page<PasdRecord> result = pasdRepository.findByEffDtAndQuery(
                    Utils.getEndDate(), query, page);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.PROCESSING);
        }
    }

    @GetMapping(path = "/upload")
    public ResponseEntity<String> uploadPasd(){
        if (state != UploadingState.UPLOADING){
            state = UploadingState.UPLOADING;
            CompletableFuture.runAsync(this::uploadAndChangeStatus);
            return new ResponseEntity<>("Началась загрузка", HttpStatus.OK);
        }
        return new ResponseEntity<>("Загрузка данных в процессе", HttpStatus.PROCESSING);
    }

    private void uploadAndChangeStatus(){
        try {
            pasdRestClient.readFromNet();
        }catch (Exception ex){
            log.error("Ошибка загрузки справочника ПАСД: " + ex.getMessage());
        }
        state = UploadingState.ACTIVE;

    }

}
