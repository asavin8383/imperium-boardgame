package model.task;

import lombok.Data;
import enums.ExecutionStatus;
import model.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Creation date: 15.06.2019
 * Author: asavin
 */
@Entity
@Table(schema = "portal", name = "arrangement_views")
@Data
public class ArrangementView implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "arrangement_id", referencedColumnName = "id")
    private Arrangement arrangement;

    /**Статус мероприятия*/
    @Enumerated(EnumType.STRING)
    @NotNull
    private ExecutionStatus status;

    private boolean viewed;
}
