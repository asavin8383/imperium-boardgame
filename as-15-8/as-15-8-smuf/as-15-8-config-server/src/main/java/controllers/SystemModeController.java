package controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.SystemMode;
import model.enums.SystemModeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.SystemModesRepository;
import services.SystemModeService;

import javax.transaction.Transactional;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/mode", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_MANAGE_FUNCTION_MODE', 'ROLE_MANAGE_CONFIGURATIONS')")
public class SystemModeController {

    private final SystemModesRepository systemModesRepository;
    private final SystemModeService systemModeService;

    @PostMapping
    public SystemModeUnit getCurrentMode(){
        return systemModesRepository.getCurrentMode()
                .orElseGet(() -> systemModesRepository.save(new SystemMode(SystemModeUnit.NORMAL, true)).getSystemMode());
    }

    @PostMapping(path = "/set")
    @PreAuthorize("hasAnyRole('ROLE_MANAGE_FUNCTION_MODE')")
    @Transactional
    public ResponseEntity setMode(@RequestBody SystemMode mode){
        SystemModeUnit curMode = systemModesRepository.getCurrentMode()
                .orElseGet(() -> systemModesRepository.save(new SystemMode(SystemModeUnit.NORMAL, false)).getSystemMode());

        systemModesRepository.findBySystemMode(curMode)
                .map(systemMode -> {
                    systemMode.setActive(false);
                    return systemModesRepository.save(systemMode);
                }).orElseGet(() -> {
                    throw new RuntimeException("Ошибка получения текущего режима");
                });

        return systemModesRepository.findBySystemMode(mode.getSystemMode())
                .map(systemMode -> {
                    if(!systemMode.isActive()){
                        systemMode.setActive(true);
                        systemModesRepository.save(systemMode);
                    }
                    return ResponseEntity.ok("Режим успешно сменен");
                })
        .orElseGet(() -> {
            systemModesRepository.save(new SystemMode(mode.getSystemMode(), true));
            return ResponseEntity.ok("Режим успешно сменен");
        });
    }

    @PutMapping(path = "/service_mode_plan")
    @PreAuthorize("hasAnyRole('ROLE_MANAGE_FUNCTION_MODE')")
    public ResponseEntity setPlannedDate(@RequestBody SystemMode mode){
        if(mode.getSystemMode() == SystemModeUnit.SERVICE && mode.getPlannedDateTime()!= null) {
            systemModeService.planServiceModeChange(mode);
            return ResponseEntity.ok("Смена сервисного режима запданирована на " + mode.getPlannedDateTime());
        }
        return ResponseEntity.badRequest().body("Смена режима на Сервисный не запланирована");
    }

}
