package model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class PSEntry
{


    public long Id;
    public String Name;
    public String Hostname;

    @JsonFormat(timezone = "GMT+03:00")
    public Date Date;

}
