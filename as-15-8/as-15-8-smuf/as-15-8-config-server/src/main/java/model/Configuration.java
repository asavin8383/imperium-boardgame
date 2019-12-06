package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;

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

    @Getter
    @Value("${spring.cloud.config.server.jdbc.default-label}")
    @Transient
    private String defaultLabel;

    @Getter
    @Value("${spring.cloud.config.server.jdbc.default-profile}")
    @Transient
    private String defaultProfile;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @EqualsAndHashCode.Include
    private Long id;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Microservice application;

    @ToString.Include
    @EqualsAndHashCode.Include
    private String profile = defaultProfile;

    @ToString.Include
    @EqualsAndHashCode.Include
    private String label = defaultLabel;

    @ManyToMany(cascade=CascadeType.ALL, mappedBy = "configurations")
    private final Set<Robot> robots = new HashSet<>();

    @OneToMany(cascade=CascadeType.ALL, mappedBy = "configuration")
    private final Set<GlobalProperty> globalProperties = new HashSet<>();
}
