package model.rest;

import lombok.ToString;
import javax.xml.bind.annotation.*;


@ToString
public class ResourceType {
    @XmlValue
    public String value;

    @XmlAttribute(required = false)
    public String ts;
}
