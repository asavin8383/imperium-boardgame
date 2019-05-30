package model.catalog;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Creation date: 30.05.2019
 * Author: asavin
 */
@Entity
@Table(schema = "portal", name = "anonymizer_parameters")
@Data
public class AnonymizerParameters extends AccessToolParameters implements Serializable {
    private static final long serialVersionUID = 1L;

    private String stubUrl;
}
