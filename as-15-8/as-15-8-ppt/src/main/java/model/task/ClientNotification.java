package model.task;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Уведомление пользователя на фронте
 * Created by san
 * Date: 23.11.2019
 */
@Entity
@Table(schema = "portal", name = "client_notifications")
@Data
public class ClientNotification implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String operator;

    private String messageText;

    private boolean viewed = false;

    private LocalDateTime creationDate = LocalDateTime.now();

    public ClientNotification(String operator, String messageText){
        this.operator = operator;
        this.messageText = messageText;
    }
}
