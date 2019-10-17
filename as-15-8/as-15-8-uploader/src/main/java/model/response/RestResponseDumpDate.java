package model.response;

import java.util.Date;


public class RestResponseDumpDate {
    public boolean status;
    public DumpResponse response;

    public Long getDumpLongDate(){
        String strDate = response.lastDumpDate;
        return Long.valueOf(strDate);
    }

    public Date getDumpDate(){
        return new Date(getDumpLongDate());
    }
}

class DumpResponse{
    public String lastDumpDate;
}
