package model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

/**
 * User: asinjavin
 * Date: 18.10.2019
 * Time: 20:07
 */
@Data
public class AddonEntry
{
    @JacksonXmlProperty(isAttribute = true)
    long id;
    String infoTypeId;
    Long visitorsCntRussia;
    Long visitorsCntWorld;
}
