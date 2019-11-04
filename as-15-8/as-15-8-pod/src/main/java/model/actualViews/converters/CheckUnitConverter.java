package model.actualViews.converters;

import checkUnits.CheckUnitType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Created by san
 * Date: 03.11.2019
 */
@Converter(autoApply = true)
public class CheckUnitConverter implements AttributeConverter<CheckUnitType, String> {
    @Override
    public String convertToDatabaseColumn(CheckUnitType checkUnitType) {
        return checkUnitType != null ? checkUnitType.propertyKey() : null;
    }

    @Override
    public CheckUnitType convertToEntityAttribute(String checkUnitType) {
        return checkUnitType != null ? CheckUnitType.fromPropertyKey(checkUnitType) : null;
    }
}
