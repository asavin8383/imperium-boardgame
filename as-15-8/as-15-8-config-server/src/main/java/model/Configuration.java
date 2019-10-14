package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(schema = "config", name = "configurations")
public class Configuration {

    @Getter
    @Value("${spring.cloud.config.server.jdbc.default-label}")
    @Transient
    private String defaultLabel;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    private String application;

    private String profile;

    private String label = defaultLabel;

    @OneToMany(cascade=CascadeType.ALL, mappedBy = "configuration")
    private final Set<Robot> robots = new HashSet<>();

    @OneToMany(cascade=CascadeType.ALL, mappedBy = "configuration")
    private final Set<GlobalProperty> globalProperties = new HashSet<>();
}
