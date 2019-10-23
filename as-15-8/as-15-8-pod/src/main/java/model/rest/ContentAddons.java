package model.rest;


import lombok.ToString;
import javax.xml.bind.annotation.*;


@XmlRootElement(name = "content")
@ToString
public class ContentAddons {

    @XmlAttribute
    public Long id;

    @XmlElement
    public String infoData;
}
