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
import repositories.ArrangementRepo;
import webClients.POD_WebClient;

import java.util.List;

/**
 * Created by san
 * Date: 03.11.2019
 */
@Slf4j
@RestController
@RequestMapping(path = "arrangements/checkUnits", produces = MediaType.APPLICATION_JSON_VALUE)
//@PreAuthorize("hasRole('ROLE_SYSTEM')")
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class ArrangementContentController {

    private final ArrangementRepo arrangementRepo;
    private final POD_WebClient pod_webClient;

    @GetMapping
    public Flux<CheckUnit> getAndSendCheckUnits(@RequestParam("id") Long arrangementId) {

        //TODO получать все остальные трафик-юниты тут же
        List<Long> contentIds = arrangementRepo.listContentIdsByArrangementId(arrangementId);
        return pod_webClient.fetchCheckUnits(contentIds);
    }

}
