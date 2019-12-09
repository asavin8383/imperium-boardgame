package model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PSEntry
{
    @JsonProperty("Id")
    private Long id;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Hostname")
    private String hostname;

    @JsonFormat(timezone = "GMT+03:00")
    @JsonProperty("Date")
    private Date date;

}
