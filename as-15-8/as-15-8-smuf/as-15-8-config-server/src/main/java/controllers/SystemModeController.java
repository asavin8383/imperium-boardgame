package controllers;

import enums.SystemModeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.SystemMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import repositories.SystemModesRepository;
import services.SystemModeService;

import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/mode", produces = MediaType.APPLICATION_JSON_VALUE)
public class SystemModeController {

    private final SystemModeService systemModeService;
    private final SystemModesRepository systemModesRepository;

    @PostMapping
    public SystemModeUnit getCurrentSystemModeUnit() {
        return systemModeService.getCurrentMode().getSystemMode();
    }

    @PostMapping(path = "/current")
    public SystemMode getCurrentMode() {
        return systemModeService.getCurrentMode();
    }

    @PostMapping(path = "/set")
    @PreAuthorize("hasAnyRole('ROLE_MANAGE_FUNCTION_MODE')")
    @Transactional
    public SystemModeUnit setMode(@RequestBody SystemMode mode){
        return systemModeService.changeSystemMode(mode).getSystemMode();
    }

    @PutMapping(path = "/service_mode_plan")
    @PreAuthorize("hasAnyRole('ROLE_MANAGE_FUNCTION_MODE')")
    public ResponseEntity planServiceModeChanging(@RequestParam String plannedDateTime) {
        return systemModeService.planServiceModeChange(plannedDateTime);
    }

    @PostMapping(path = "/any")
    public ResponseEntity getMode(@RequestParam String systemModeUnit){
        try {
            Optional<SystemMode> mode = systemModesRepository.findBySystemMode(SystemModeUnit.valueOf(systemModeUnit));
            if (mode.isPresent())
                return ResponseEntity.ok(mode.get());
            else
                return ResponseEntity.badRequest().body("Не удалось получить информацию о режиме работы :" + systemModeUnit);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Не удалось получить информацию о режиме работы :" + ex);
        }
    }

    @PostMapping(path = "/service_mode_cancel")
    public ResponseEntity serviceModeCancel() {
        try {
            systemModeService.cancelSystemModeSchedule();
            return ResponseEntity.ok("Запаланированный переход в сервисный режим отменён");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Запланированный переход в сервисный режим не отменён!");
        }
    }

}
