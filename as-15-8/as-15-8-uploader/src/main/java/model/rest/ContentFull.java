package model.rest;


import lombok.ToString;
import parsers.DateTimeZoneSimpleAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;
import java.util.List;


@XmlRootElement(name="content")
@ToString(callSuper = true)
public class ContentFull extends ContentRest {

    @XmlAttribute
    @XmlJavaTypeAdapter(DateTimeZoneSimpleAdapter.class)
    public Date includeTime;

    @XmlAttribute(required = false)
    public Integer urgencyType;

    @XmlAttribute(name = "entryType")
    public Long entryTypeId;

    @XmlAttribute(required = false)
    public String hash;

    @XmlAttribute(required = false)
    public String blockType;

    @XmlAttribute(required = false)
    @XmlJavaTypeAdapter(DateTimeZoneSimpleAdapter.class)
    public Date ts;

    @XmlElement
    public Decision decision;

    @XmlElements({
            @XmlElement(name = "ip", type = TypeIp.class),
            @XmlElement(name = "ipv6", type = TypeIp6.class),
            @XmlElement(name = "domain", type = TypeDomain.class),
            @XmlElement(name = "url", type = TypeUrl.class)
    })
    public List<ResourceType> types;

}
