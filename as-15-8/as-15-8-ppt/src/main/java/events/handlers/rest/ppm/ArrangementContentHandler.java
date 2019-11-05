package events.handlers.rest.ppm;

import checkUnits.CheckUnit;
import events.producers.rest.pod.CheckUnitUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.task.Arrangement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import services.arrangement.impl.ArrangementService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by san
 * Date: 03.11.2019
 */
@Slf4j
@RestController
@RequestMapping(path = "arrangements/checkUnits", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_SYSTEM')")
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class ArrangementContentHandler {

    private final ArrangementService arrangementService;
    private final CheckUnitUploader checkUnitUploader;

    @GetMapping
    public List<CheckUnit> getAndSendCheckUnits(@RequestParam("id") Long arrangementId) {
        Arrangement arrangement = arrangementService.getById(arrangementId);
        List<CheckUnit> checkUnits = new ArrayList<>();
        //TODO получать все остальные трафик-юниты тут же
        arrangement.getTraffic().getErdiTrafficUnits()
            .forEach(erdiTrafficUnit -> erdiTrafficUnit.getFormalErdiList()
                .forEach(erdiContentJoin ->
                    checkUnits.addAll(checkUnitUploader.getCheckUnitsByContentId(erdiContentJoin.getContentId()))
                        )
            );
        return checkUnits;
    }

}
