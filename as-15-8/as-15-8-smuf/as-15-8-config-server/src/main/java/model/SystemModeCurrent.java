package model;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@Table(schema = "config", name = "system_modes_current")
@NoArgsConstructor
@RequiredArgsConstructor
public class SystemModeCurrent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(Views.Brief.class)
    private Long id;

    @NonNull
    @Column(nullable = false)
    private Long systemModeCurrent;

}
