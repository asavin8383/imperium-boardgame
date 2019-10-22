package model.rest;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import model.response.AddonEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * User: asinjavin
 * Date: 18.10.2019
 * Time: 18:05
 */
@Data
@JacksonXmlRootElement(namespace = "reg", localName = "register")
public class RestResponseAddon
{
    @JacksonXmlElementWrapper(localName = "content", useWrapping = false)
    List<AddonEntry> content = new ArrayList<>();

}


