package controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rest.ActRequest;
import rest.ResponseStatusString;
import services.ActService;

@RestController
@RequestMapping(value = "/act", produces =  MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ActController {

    private final ActService actService;

    @PostMapping
    //@PreAuthorize("hasAnyRole('ROLE_SYSTEM')")
    public ResponseEntity<Void> createAct(@RequestBody ActRequest actRequest){
        ResponseStatusString resp = actService.createAct(actRequest);
        return new ResponseEntity<>(resp.isStatus() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
