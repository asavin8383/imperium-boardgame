package controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.SystemMode;
import model.SystemModeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import repositories.SystemModesRepository;

import javax.transaction.Transactional;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/mode", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_MANAGE_FUNCTION_MODE', 'ROLE_MANAGE_CONFIGURATIONS')")
public class SystemModeController {

    private final SystemModesRepository systemModesRepository;

    @PostMapping
    public SystemModeUnit getCurrentMode(){
        return systemModesRepository.getCurrentMode().orElse(SystemModeUnit.NORMAL);
    }

    @PostMapping(path = "/set")
    @PreAuthorize("hasAnyRole('ROLE_MANAGE_FUNCTION_MODE')")
    @Transactional
    public ResponseEntity setMode(@RequestBody SystemModeUnit mode){
        SystemModeUnit curMode = systemModesRepository.getCurrentMode()
                .orElseGet(() -> {
                    throw new RuntimeException("Ошибка получения текущего режима");
                });
        systemModesRepository.findBySystemMode(curMode)
                .map(systemMode -> {
                    systemMode.setActive(false);
                    return systemModesRepository.save(systemMode);
                }).orElseGet(() -> {
                    throw new RuntimeException("Ошибка получения текущего режима");
                });

        return systemModesRepository.findBySystemMode(mode)
                .map(systemMode -> {
                    if(!systemMode.isActive()){
                        systemMode.setActive(true);
                        systemModesRepository.save(systemMode);
                    }
                    return ResponseEntity.ok("Режим успешно сменен");
                })
        .orElseGet(() -> {
            systemModesRepository.save(new SystemMode(mode, true));
            return ResponseEntity.ok("Режим успешно сменен");
        });
    }

}
