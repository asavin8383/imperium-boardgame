package model.parameters;

import enums.AccessToolParameters;
import lombok.Data;

import javax.persistence.*;

/**
 * Creation date: 03.06.2019
 * Author: asavin
 * Глобальные параметры системы
 */

@Entity
@Table(schema = "portal", name = "global_parameters")
@Data
public class GlobalParameter {

    @Id
    @Enumerated(EnumType.STRING)
    private AccessToolParameters key;

    private String value;

}
