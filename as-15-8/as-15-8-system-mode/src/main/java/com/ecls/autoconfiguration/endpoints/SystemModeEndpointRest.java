package com.ecls.autoconfiguration.endpoints;

import com.ecls.autoconfiguration.model.CurrentSystemMode;
import enums.SystemModeUnit;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@RestControllerEndpoint(id = "system-mode")
public class SystemModeEndpointRest {

    @GetMapping
    public String getSystemMode() {
        return "from rest";
    }

    @PostMapping
    public ResponseEntity<SystemModeUnit> postSystemMode(@RequestBody SystemModeUnit systemModeUnit) {
        if (systemModeUnit != null) {
            CurrentSystemMode.setSystemModeUnit(systemModeUnit);
            return ResponseEntity.ok(systemModeUnit);
        } else return ResponseEntity.badRequest().body(null);
    }

}
