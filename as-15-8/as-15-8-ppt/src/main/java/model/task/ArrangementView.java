package model.task;

import enums.ExecutionStatus;
import lombok.Data;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String operator;

    @ManyToOne(optional = false)
    @JoinColumn(name = "arrangement_id", referencedColumnName = "id")
    private Arrangement arrangement;

    /**Статус мероприятия*/
    @Enumerated(EnumType.STRING)
    @NotNull
    private ExecutionStatus status;

    private boolean viewed;
}
