package model.converters;

import model.enums.UrgencyType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;


@Converter(autoApply = true)
public class UrgencyTypeConverter implements AttributeConverter<UrgencyType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(UrgencyType urgencyType) {
        return urgencyType == null ? null : Integer.parseInt(urgencyType.getValue());
    }

    @Override
    public UrgencyType convertToEntityAttribute(Integer urgencyType) {
        return UrgencyType.parse(urgencyType);
    }
}
