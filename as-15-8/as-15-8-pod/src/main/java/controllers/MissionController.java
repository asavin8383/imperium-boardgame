package controllers;


import controllers.enums.UploadingState;
import controllers.utils.SortingDirection;
import controllers.utils.SortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.scheme.Mission;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import repositories.MissionRepository;
import services.MissionService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
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
            return new ResponseEntity<>((Page<Mission>) null, HttpStatus.ACCEPTED);
        }
    }

    @GetMapping(path = "/get_image")
    public @ResponseBody
    ResponseEntity<byte[]> getOriginalMissionDocument(@RequestParam long id) throws IOException {
        byte[] result = missionService.receiveMissionDocumentFromDB(id);
        if (result != null && result.length > 0) {

            Detector detector = new DefaultDetector();
            Metadata metadata = new Metadata();
            String mime = detector.detect(new ByteArrayInputStream(result), metadata).toString();
            log.info("Запрошен документ оригинального поручения: "+id+" Тип документа определен как "+mime);

            HttpHeaders responseHeaders = new HttpHeaders();
            //String mime = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(result));
            if (mime == null) mime = "application/octet-stream";
            responseHeaders.setContentType(MediaType.parseMediaType(mime));
            responseHeaders.setContentDisposition(ContentDisposition.parse("inline"));
            log.debug("responseHeaders {}", responseHeaders.toString());
            return new ResponseEntity<>(result, responseHeaders, HttpStatus.OK);
        }
        return new ResponseEntity<>((byte[]) null, HttpStatus.NO_CONTENT);

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
