package model.rest;


import lombok.ToString;

import javax.xml.bind.annotation.*;
import java.util.List;


@XmlRootElement(name="content")
@ToString
public class ContentFull {
    @XmlAttribute
    public Long id;

    @XmlAttribute
    public String includeTime;

    @XmlAttribute(required = false)
    public String urgencyType;

    @XmlAttribute
    public String entryType;

    @XmlAttribute(required = false)
    public String hash;

    @XmlAttribute(required = false)
    public String blockType;

    @XmlAttribute(required = false)
    public String ts;

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
