package model.actualViews;

import checkUnits.CheckUnitType;
import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.Data;
import model.actualViews.converters.CheckUnitConverter;

import javax.persistence.*;

/**
 * Created by san
 * Date: 03.11.2019
 */
@Immutable
@Entity
@Table(schema = "sor", name = "check_units")
@Data
public class ContentCheckUnit {

    @Id
    private Long checkUnitId;

    private Long contentId;

    @Convert(converter = CheckUnitConverter.class)
    private CheckUnitType checkUnitType;

    private String checkUnitValue;

}
