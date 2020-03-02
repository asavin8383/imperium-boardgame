package controllers.ppm;

import checkUnits.CheckUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import services.arrangement.impl.ArrangementService;

import java.util.List;

/**
 * Created by san
 * Date: 03.11.2019
 */
@Slf4j
@RestController
@RequestMapping(path = "arrangements/checkUnits", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class ArrangementContentController {

    private final ArrangementService arrangementService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<List<CheckUnit>> getAndSendCheckUnits(@RequestParam("id") Long arrangementId) {

       return arrangementService.getCheckUnitsFromPod(arrangementId);

    }

}
