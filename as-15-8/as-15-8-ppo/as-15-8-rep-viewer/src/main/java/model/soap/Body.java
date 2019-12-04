package model.soap;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

/**
 * User: asinjavin
 * Date: 22.11.2019
 * Time: 16:50
 */
@Data
public class Body
{
    @JacksonXmlProperty(namespace = "birt")
    model.soap.GetUpdatedObjects GetUpdatedObjects;
}
