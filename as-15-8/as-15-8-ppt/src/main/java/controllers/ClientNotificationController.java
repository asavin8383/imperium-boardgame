package controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.task.ClientNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ClientNotificationRepo;

import java.security.Principal;
import java.util.List;

/**
 * Created by san
 * Date: 23.11.2019
 */
@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/client_notifications", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_FORMAL_TASK')")
public class ClientNotificationController {

    private final ClientNotificationRepo clientNotificationRepo;

    @GetMapping
    public List<ClientNotification> findList(@RequestParam boolean viewed, Principal principal){
        log.info("Попытка поиска нотификейшена ", principal);
        log.info("clientNotification.getOperator = " + principal.getName());

        return clientNotificationRepo.findAllByOperatorAndViewed(principal.getName(), viewed);
    }

    @PutMapping
    public ClientNotification updateClientNotification(@RequestParam("id") ClientNotification clientNotification){
        clientNotification.setViewed(true);
        return clientNotificationRepo.save(clientNotification);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void deleteClientNotification(@RequestParam("id") ClientNotification clientNotification){
        clientNotificationRepo.delete(clientNotification);
    }

}
