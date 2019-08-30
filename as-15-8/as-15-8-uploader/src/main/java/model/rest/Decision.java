package model.rest;

import lombok.ToString;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="decision")
@ToString
public class Decision {
    @XmlAttribute
    String date;

    @XmlAttribute
    String number;

    @XmlAttribute
    String org;
}
