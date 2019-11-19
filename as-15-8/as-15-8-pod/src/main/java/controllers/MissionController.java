package controllers;


import controllers.enums.UploadingState;
import controllers.utils.SortingDirection;
import controllers.utils.SortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.scheme.Mission;
import org.apache.logging.log4j.util.Strings;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypeException;
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
    public @ResponseBody ResponseEntity<byte[]> getOriginalMissionDocument(
            @RequestParam("id") Mission mission) throws IOException, MimeTypeException {
        byte[] result = missionService.receiveMissionDocumentFromDB(mission.getId());
        if (result != null && result.length > 0) {

            Detector detector = new DefaultDetector();
            Metadata metadata = new Metadata();
            String mime = detector.detect(new ByteArrayInputStream(result), metadata).toString();
            String documentName = "Поручение " + mission.getOrigId();
            if (Strings.isEmpty(mime)) {
                mime = "application/octet-stream";
            } else {
                String documentExtension = TikaConfig.getDefaultConfig().getMimeRepository().forName(mime).getExtension();
                documentName += "." + documentExtension;
            }

            log.info("Запрошен документ оригинального поручения: "+mission.getOrigId() +
                    " Тип документа определен как " + mime +
                    " Сформировано имя документа: " + documentName);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.parseMediaType(mime));
            responseHeaders.setContentDisposition(ContentDisposition.parse("inline"));
            responseHeaders.set("Title", documentName);
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
