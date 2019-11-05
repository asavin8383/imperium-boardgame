package model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PSEntry
{


    public Long Id;
    public String Name;
    public String Hostname;

    @JsonFormat(timezone = "GMT+03:00")
    public Date Date;

}
