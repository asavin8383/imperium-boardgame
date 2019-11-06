package controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import services.ActService;


@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ActController {

    private final ActService actService;


    @PreAuthorize("hasAnyRole('ROLE_SYSTEM')")
    @GetMapping(path = "/create_act", produces = MediaType.APPLICATION_JSON_VALUE)
    public String update(
            @RequestParam(defaultValue = "") Long arragement_id
    ){
        boolean res = actService.createAct(arragement_id);
        return ""+res;
    }

}
