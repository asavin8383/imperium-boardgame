package model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class PASDEntry
{
    public long Id;

    public String Name;
    public String Hostname;
    public String DomainNames;
    public String ServiceDescription;
    public String NetworkAddresses;
    public String IpAccessFgis;
    public String Credentials;

    @JsonFormat(timezone = "GMT+03:00")
    public Date Date;

}
