package model.response;

import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class RestResponseDumpDate {
    public boolean status;
    public DumpResponse response;

    public Date getDumpDate() throws ParseException {
        String strDate = response.lastDumpDate;
        if (StringUtils.isEmpty(strDate)){
            return null;
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return dateFormat.parse(strDate);
    }
}

class DumpResponse{
    public String lastDumpDate;
}
