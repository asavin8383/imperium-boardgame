package model.soap;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import org.eclipse.birt.report.soapengine.api.Oprand;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * User: asinjavin
 * Date: 22.11.2019
 * Time: 14:23
 */
@Data
public class Envelope
{
    @JacksonXmlProperty(namespace = "soap")
    Body Body;
}



