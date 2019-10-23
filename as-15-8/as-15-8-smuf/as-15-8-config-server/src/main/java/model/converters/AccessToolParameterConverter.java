package model.converters;

import enums.AccessToolParameter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class AccessToolParameterConverter implements AttributeConverter<AccessToolParameter, String> {
    @Override
    public String convertToDatabaseColumn(AccessToolParameter accessToolParameter) {
        return accessToolParameter.propertyKey();
    }

    @Override
    public AccessToolParameter convertToEntityAttribute(String accessToolParameter) {
        return AccessToolParameter.fromPropertyKey(accessToolParameter);
    }
}
