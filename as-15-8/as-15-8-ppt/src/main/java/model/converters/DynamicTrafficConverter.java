package model.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.List;


@Converter
public class DynamicTrafficConverter  implements AttributeConverter<List<String>, String> {

    private static final String splitter = ", ";

    @Override
    public String convertToDatabaseColumn(List<String> stringList) {
        if (stringList != null)
            return String.join(splitter, stringList);
         else return null;

    }

    @Override
    public List<String> convertToEntityAttribute(String listStr) {
        if (listStr !=null)
            return Arrays.asList(listStr.split(splitter));
        else return null;
    }
}
