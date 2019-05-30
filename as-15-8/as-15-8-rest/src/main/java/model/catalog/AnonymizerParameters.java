package model.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Creation date: 30.05.2019
 * Author: asavin
 */
@Entity
@Table(schema = "portal", name = "anonymizer_parameters")
@Data
public class AnonymizerParameters implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "anonymizer_parameters_generator")
    @SequenceGenerator(name="anonymizer_parameters_generator", schema = "portal", sequenceName = "anonymizer_parameters_id_seq", allocationSize=1)
    @Column(name="id", nullable=false, updatable=false)
    private Long id;

    @OneToOne(optional = false, cascade=CascadeType.ALL)
    @JsonIgnore
    private AccessTool accessTool;

    private String stubUrl;
}
