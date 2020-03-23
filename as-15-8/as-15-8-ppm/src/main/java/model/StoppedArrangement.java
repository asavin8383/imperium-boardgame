package model;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**Вспомогательная таблица для расписания. Позволяет видеть остановленные мероприятия,
 * причину остановки и процент выполнения*/

@Entity
@Table(schema="schedule", name="stopped_arrangements")
@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

public class StoppedArrangement {
    @Id
    @JsonView(Views.Id.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long arrangementId;

    @NotNull
    @JsonView(Views.Brief.class)
    @ToString.Include
    private Long completionPerscent;

    @NotNull
    @JsonView(Views.Brief.class)
    @ToString.Include
    private String stoppingReason;

    @ManyToOne(optional=false)
    @JoinColumn(name="schedule_id")
    @NonNull
    private Schedule schedule;

    public StoppedArrangement() {

    }
}
