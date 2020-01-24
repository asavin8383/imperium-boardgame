package model.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


@ToString
public class DeltaIdEntry {
    public long deltaId;
    public Date actualDate;
    public boolean isEmpty;

    @JsonCreator
    public DeltaIdEntry(@JsonProperty("deltaId") String deltaId,
                        @JsonProperty("actualDate") String actualDate,
                        @JsonProperty("isEmpty") String isEmpty) throws ParseException {
        super();

        this.deltaId = Long.valueOf(deltaId);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        //dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        this.actualDate = dateFormat.parse(actualDate);

        this.isEmpty = "1".equals(isEmpty);
    }
}
