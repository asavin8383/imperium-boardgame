package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(schema = "config", name = "configurations")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Configuration {

    @Getter
    @Value("${spring.cloud.config.server.jdbc.default-label}")
    @Transient
    private String defaultLabel;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @EqualsAndHashCode.Include
    private Long id;

    @ToString.Include
    @EqualsAndHashCode.Include
    private String application;

    @ToString.Include
    @EqualsAndHashCode.Include
    private String profile;

    @ToString.Include
    @EqualsAndHashCode.Include
    private String label = defaultLabel;

    @ManyToMany(cascade=CascadeType.ALL, mappedBy = "configurations")
    private final Set<Robot> robots = new HashSet<>();

    @OneToMany(cascade=CascadeType.ALL, mappedBy = "configuration")
    private final Set<GlobalProperty> globalProperties = new HashSet<>();
}
