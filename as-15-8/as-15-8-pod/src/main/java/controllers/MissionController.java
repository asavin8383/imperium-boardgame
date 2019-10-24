package controllers;


import controllers.enums.UploadingState;
import controllers.utils.SortingDirection;
import controllers.utils.SortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.scheme.Mission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import repositories.MissionRepository;
import services.MissionService;

import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping(path = "/mission", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class MissionController {

    private final MissionService missionService;
    private final MissionRepository missionRepository;

    private UploadingState state = UploadingState.ACTIVE;


    @GetMapping
    public ResponseEntity<Page<Mission>> getSearchSystemPage(@RequestParam(required = false) SortingDirection sortingDirection,
                                                             @RequestParam(required = false) String sortingColumn,
                                                             @RequestParam(defaultValue = "0") int pageNumber,
                                                             @RequestParam(defaultValue = "10") int pageSize,
                                                             @RequestParam(required = false) String query)
    {
        if (state != UploadingState.UPLOADING) {
            Pageable page = PageRequest.of(pageNumber, pageSize,
                    SortingHelper.createSorting(sortingDirection, sortingColumn));
            Page<Mission> result = missionRepository.findByQuery(query, page);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.ACCEPTED);
        }
    }

    @GetMapping(path = "/get_image", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    ResponseEntity<byte[]> getPdf(@RequestParam String id){
        return missionService.receivePdfFromDB(id);
    }

    @GetMapping(path = "/load")
    public ResponseEntity<String> update() {
        if (state != UploadingState.UPLOADING) {
            CompletableFuture.runAsync(() -> {
                try{
                    missionService.fillMissions();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    state = UploadingState.ACTIVE;
                }

            });
            return new ResponseEntity<>("Началась загрузка поручений", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Выполняется загрузка поручений", HttpStatus.PROCESSING);
        }
    }

}
