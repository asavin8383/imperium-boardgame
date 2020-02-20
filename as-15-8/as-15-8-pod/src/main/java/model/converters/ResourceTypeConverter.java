package model.converters;

import checkUnits.CheckUnitType;
import exceptions.AS_15_8_POD_Exception;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// todo - переделать! выглядить ужасно!

@Converter(autoApply = true)
public class ResourceTypeConverter implements AttributeConverter<CheckUnitType, Integer> {

    private static Map<CheckUnitType, Integer> map = new HashMap<>();
    static {
        map.put(CheckUnitType.DOMAIN,       1);
        map.put(CheckUnitType.IP_V4,        2);
        map.put(CheckUnitType.IP_V6,        3);
        map.put(CheckUnitType.IP_V4_SUBNET, 4);
        map.put(CheckUnitType.IP_V6_SUBNET, 5);
        map.put(CheckUnitType.URL,          6);
        map.put(CheckUnitType.DOMAIN_MASK,  7);
    }

    @Override
    public Integer convertToDatabaseColumn(CheckUnitType checkUnitType) {
        return typeToInt(checkUnitType);
    }

    @Override
    public CheckUnitType convertToEntityAttribute(Integer checkUnitTypeId) {
        return intToType(checkUnitTypeId);
    }

    public static Integer typeToInt(CheckUnitType checkUnitType) {
        if (checkUnitType == null)
            checkUnitType = CheckUnitType.URL;

        Integer result = map.get(checkUnitType);
        if (result == null)
            throw new AS_15_8_POD_Exception("Невозможно определить checkUnitTypeId по checkUnitType = " + checkUnitType.name());

        return map.get(checkUnitType);
    }

    public static CheckUnitType intToType(Integer checkUnitTypeId) {
        if (checkUnitTypeId == null)
            checkUnitTypeId = map.get(CheckUnitType.URL);

        CheckUnitType result = null;
        for(CheckUnitType checkUnitType : map.keySet()){
            Integer id = map.get(checkUnitType);
            if (id.equals(checkUnitTypeId)){
                result = checkUnitType;
            }
        }

        if (result == null)
            throw new AS_15_8_POD_Exception("Невозможно определить checkUnitType по checkUnitTypeId = " + checkUnitTypeId);

        return result;
    }

    public static List<Integer> typeToIntList(CheckUnitType ...list) {
        List<Integer> res = new ArrayList<>();
        for (CheckUnitType type : list){
            res.add(typeToInt(type));
        }
        return res;
    }

    public static List<CheckUnitType> intToTypeList(Integer ...list) {
        List<CheckUnitType> res = new ArrayList<>();
        for (Integer id : list){
            res.add(intToType(id));
        }
        return res;
    }
}
