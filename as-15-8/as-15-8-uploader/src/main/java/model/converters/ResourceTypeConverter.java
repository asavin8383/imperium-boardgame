package model.converters;

import checkUnits.CheckUnitType;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;


// todo - переделать! выглядить ужасно!

@Converter(autoApply = true)
public class ResourceTypeConverter implements AttributeConverter<CheckUnitType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(CheckUnitType checkUnitType) {
        if (checkUnitType == null)
            return 1;

        switch (checkUnitType){
            case URL:
                return 6;
            case DOMAIN:
                return 1;
            case DOMAIN_MASK:
                return 1;       // nope
            case IP_V4:
                return 2;
            case IP_V6:
                return 3;
            case IP_V4_SUBNET:
                return 4;
            case IP_V6_SUBNET:
                return 5;
        }
        return 1;
    }

    @Override
    public CheckUnitType convertToEntityAttribute(Integer checkUnitType) {
        if (checkUnitType == null)
            return CheckUnitType.DOMAIN;

        switch (checkUnitType){
            case 0:
                return CheckUnitType.URL;
            case 1:
                return CheckUnitType.DOMAIN;
            case 2:
                return CheckUnitType.IP_V4;
            case 3:
                return CheckUnitType.IP_V6;
            case 4:
                return CheckUnitType.IP_V4_SUBNET;
            case 5:
                return CheckUnitType.IP_V6_SUBNET;
            case 6:
                return CheckUnitType.URL;
        }

        return CheckUnitType.DOMAIN;
    }
}
