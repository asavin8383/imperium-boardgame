package model.rest;


import lombok.ToString;
import parsers.DateTimeZoneSimpleAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


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
            @XmlElement(name = "url", type = TypeUrl.class),
            @XmlElement(name = "ipSubnet", type = TypeIpSubnet.class),
            @XmlElement(name = "ipv6Subnet", type = TypeIp6Subnet.class)
    })
    private List<ResourceType> types;
    private boolean wasFilteredTypes = false;

    public List<ResourceType> getTypes(){
        if (types == null)
            types = new ArrayList<>();

        if (!wasFilteredTypes){
            wasFilteredTypes = true;

            types = types.stream().map(resourceType -> {
                // создаем доменную маску
                if (resourceType instanceof TypeDomain){
                    String value = resourceType.value == null ? "" : resourceType.value;
                    if (value.contains("*")){
                        return new TypeDomainMask(resourceType.value, resourceType.ts);
                    }
                }
                return resourceType;
            }).collect(Collectors.toList());
        }
        return types;
    }

}
