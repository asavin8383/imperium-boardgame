package model.rest;


import lombok.ToString;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="delete")
@ToString(callSuper = true)
public class ContentDelete extends ContentRest {
}
