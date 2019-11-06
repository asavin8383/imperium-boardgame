package controllers.traffic;

import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.enums.TrafficUnitType;
import model.traffic.ErdiTrafficUnit;
import model.traffic.ErdiTrafficUnitContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.CustomErdiRepository;
import repositories.ErdiContentJoinRepository;
import repositories.ErdiTrafficUnitRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/traffic/unit/erdi",
        produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ErdiTrafficUnitController {

    private final ErdiTrafficUnitRepository erdiTrafficUnitRepository;
    private final CustomErdiRepository customErdiRepository;
    private final ErdiContentJoinRepository erdiContentJoinRepository;

    @PutMapping(path = "/{id}/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addErdiToUnit(@PathVariable("id") ErdiTrafficUnit unit, @RequestBody List<Long> ids) {
        if (unit == null)
            throw new AS_15_8_PPT_Exception("ErdiTrafficUnit not found");

        if (unit.getType() == TrafficUnitType.FORMAL) {
            List<ErdiTrafficUnitContent> records = ids.stream()
                    .map(id -> new ErdiTrafficUnitContent(unit, id))
                    .collect(Collectors.toList());
            unit.getFormalErdiList().addAll(records);
        } else {
            // assert unit.getType() == TrafficUnitType.CUSTOM
            unit.getCustomErdiList().addAll(customErdiRepository.findAllById(ids));
        }
        erdiTrafficUnitRepository.save(unit);
    }

    @PutMapping(path = "/{id}/remove", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void removeErdiFromUnit(@PathVariable("id") ErdiTrafficUnit unit, @RequestBody List<Long> ids) {
        if (unit == null)
            throw new AS_15_8_PPT_Exception("ErdiTrafficUnit not found");

        if (unit.getType() == TrafficUnitType.FORMAL) {
            unit.getFormalErdiList().removeAll(
                    erdiContentJoinRepository.findAllByTrafficUnitAndContentIdIn(unit, ids));
        } else {
            // assert unit.getType() == TrafficUnitType.CUSTOM
             unit.getCustomErdiList().removeAll(customErdiRepository.findAllById(ids));
        }
        erdiTrafficUnitRepository.save(unit);
    }

}
