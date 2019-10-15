package model.rest;

import lombok.ToString;
import parsers.DateSimpleAdapter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;


@XmlType(name="decision")
@ToString
public class Decision {

    @XmlAttribute
    @XmlJavaTypeAdapter(DateSimpleAdapter.class)
    public Date date;

    @XmlAttribute
    public String number;

    @XmlAttribute
    public String org;
}
