package model.response;


import model.rest.SubType;
import java.util.List;


public class RestResponseSubTypeList {
    public boolean status;
    public SubTypeResponse response;

    public List<SubType> subTypeList(){
        return response.Records;
    }
}

class SubTypeResponse {
    public java.util.Date Date;
    public List<SubType> Records;
}