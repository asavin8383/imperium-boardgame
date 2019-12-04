package model.soap;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User: asinjavin
 * Date: 22.11.2019
 * Time: 16:50
 */
@Data
public class SoapOperation
{
    SoapTarget Target;
    String Operator;
    @JacksonXmlProperty
    @JacksonXmlElementWrapper(useWrapping = false)
    List<SoapOprand> Oprand;


    public Map<String, String> params() {
        HashMap<String, String> map = new HashMap<>();
        for (SoapOprand oprand : Oprand)
            map.put(oprand.getName(), oprand.getValue());
        return map;
    }
}
