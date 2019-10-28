package model.converters;

import enums.AccessToolUnit;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class AccessToolUnitConverter implements AttributeConverter<AccessToolUnit, String> {
    @Override
    public String convertToDatabaseColumn(AccessToolUnit accessToolUnit) {
        return accessToolUnit != null ? accessToolUnit.propertyKey() : null;
    }

    @Override
    public AccessToolUnit convertToEntityAttribute(String accessToolUnit) {
        return accessToolUnit != null ? AccessToolUnit.fromPropertyKey(accessToolUnit) : null;
    }
}
