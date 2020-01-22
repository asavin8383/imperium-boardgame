package model.response;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.ToString;

import java.io.UnsupportedEncodingException;

@ToString
public class RestResponseStatusString {
    public boolean status;
    public String response;

    public static RestResponseStatusString getFromData(byte[] data){
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(data, RestResponseStatusString.class);
        }
        catch (Exception e) {
            return null;
        }
    }
    public static RestResponseStatusString getFromData(String data){
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(data, RestResponseStatusString.class);
        }
        catch (Exception e) {
            return null;
        }
    }
    public static RestResponseStatusString getFromData(String data, String charSet){
        try {
            return getFromData(data.getBytes(charSet));
        } catch (UnsupportedEncodingException e) {
           return null;
        }
    }

}
