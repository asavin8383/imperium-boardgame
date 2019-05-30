package model.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

/**
 * Creation date: 30.05.2019
 * Author: asavin
 */
@MappedSuperclass
@Data
public abstract class AccessToolParameters {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name="id", nullable=false, updatable=false)
    private Long id;

    @OneToOne(optional = false, cascade= CascadeType.ALL)
    @JsonIgnore
    private AccessTool accessTool;
}
