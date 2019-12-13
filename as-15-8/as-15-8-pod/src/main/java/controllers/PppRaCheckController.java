package controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import restapi.PPPRACheckClient;

@RestController
@RequestMapping(path = "/pppracheck")
@PreAuthorize("hasAnyRole('ROLE_MANAGE_CONFIGURATIONS')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)

public class PppRaCheckController {

    private final PPPRACheckClient ppraCheckClient;

    @GetMapping
    public boolean isRegistryAvailable() {
        return ppraCheckClient.checkRegistryIsAvailable();
    }
}
