package model;

import com.fasterxml.jackson.annotation.JsonView;
import model.enums.Microservice;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(schema = "config", name = "configurations")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Configuration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @JsonView(Views.Brief.class)
    private Long id;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    @EqualsAndHashCode.Include
    @JsonView(Views.Brief.class)
    private Microservice application;

    @NonNull
    @ToString.Include
    @EqualsAndHashCode.Include
    private String profile;

    @NonNull
    @ToString.Include
    @EqualsAndHashCode.Include
    private String label;

    @ManyToMany(cascade=CascadeType.ALL, mappedBy = "configurations")
    private final Set<Robot> robots = new HashSet<>();

    @OneToMany(cascade=CascadeType.ALL, mappedBy = "configuration")
    private final Set<GlobalProperty> globalProperties = new HashSet<>();
}
