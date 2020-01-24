package model.rest;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRest {
    public Date updateTime;
    public Date updateTimeUrgently;
    public String formatVersion;
    public String reg;
    public String tns;

    public RegisterRest(String updateTime, String updateTimeUrgently, String formatVersion, String reg, String tns) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");  // тайм зону не учитываем!!!

        try {
            this.updateTime = updateTime == null ? null : dateFormat.parse(updateTime);
        } catch (ParseException e) {
            e.printStackTrace();
            this.updateTime = null;
        }
        try {
            this.updateTimeUrgently = updateTimeUrgently == null ? null : dateFormat.parse(updateTimeUrgently);
        } catch (ParseException e) {
            e.printStackTrace();
            this.updateTimeUrgently = null;
        }

        this.formatVersion = formatVersion;
        this.reg = reg;
        this.tns = tns;
    }
}
