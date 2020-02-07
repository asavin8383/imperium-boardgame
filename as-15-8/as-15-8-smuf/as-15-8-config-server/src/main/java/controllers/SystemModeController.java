package controllers;

import enums.SystemModeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.SystemMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import services.SystemModeService;

import javax.transaction.Transactional;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/mode", produces = MediaType.APPLICATION_JSON_VALUE)
public class SystemModeController {

    private final SystemModeService systemModeService;

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
        systemModeService.setCurrentSystemModeDisabled();
        systemModeService.notifyAllApplications(mode.getSystemMode());
        return systemModeService.changeSystemMode(mode).getSystemMode();
    }

    @PutMapping(path = "/service_mode_plan")
    @PreAuthorize("hasAnyRole('ROLE_MANAGE_FUNCTION_MODE')")
    public ResponseEntity planServiceModeChanging(@RequestParam String plannedDateTime) {
        return systemModeService.planServiceModeChange(plannedDateTime);
    }

}
