package model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class PASDEntry
{
    @JsonProperty("Id")
    private Long id;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Hostname")
    private String hostname;
    @JsonProperty("DomainNames")
    private String domainNames;
    @JsonProperty("ServiceDescription")
    private String serviceDescription;
    @JsonProperty("NetworkAddresses")
    private String networkAddresses;
    @JsonProperty("IpAccessFgis")
    private String ipAccessFgis;
    @JsonProperty("Credentials")
    private String credentials;

    @JsonFormat(timezone = "GMT+03:00")
    @JsonProperty("Date")
    private Date date;

}
