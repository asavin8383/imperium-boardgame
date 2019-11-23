package services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.task.ClientNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.ClientNotificationRepo;

/**
 * Created by san
 * Date: 23.11.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ClientNotificationService {
    private final ClientNotificationRepo clientNotificationRepo;

    public void saveNotification(String operator, String messageText){
        ClientNotification clientNotification = new ClientNotification(operator, messageText);
        clientNotificationRepo.save(clientNotification);
    }
}
