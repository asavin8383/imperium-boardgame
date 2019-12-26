package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import model.enums.ProcessingTimeCheckMethod;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(schema = "config", name = "processing_times")
@NoArgsConstructor
public class ProcessingTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column (insertable = false, updatable = false)
    private Long robot_id;

    @NotNull
    @Column
    @Enumerated(EnumType.STRING)
    private ProcessingTimeCheckMethod check_method;

    @NotNull
    @Column
    private Long processing_time;

    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "robots_processing_time_id_FK"))
    @JsonIgnore
    private Robot robot;

}
