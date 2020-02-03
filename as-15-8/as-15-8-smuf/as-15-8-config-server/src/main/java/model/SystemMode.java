package model;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import model.enums.SystemModeUnit;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(schema = "config", name = "system_modes")
@NoArgsConstructor
@RequiredArgsConstructor
public class SystemMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(Views.Brief.class)
    private Long id;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SystemModeUnit systemMode;

    @NonNull
    @Column(nullable = false)
    private boolean active;

    @Column
    private LocalDateTime plannedDateTime;
}
