package model.rest;


import lombok.ToString;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;



@ToString
public class ContentRest {

    @XmlAttribute
    public Long id;

}
