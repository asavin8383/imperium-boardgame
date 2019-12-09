package model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class DomainMasklItemEntry {

    public Long id;
    public String domainMask;
    public String domainMaskItem;

    @JsonFormat(timezone = "GMT+03:00")
    public Date Date;

}

