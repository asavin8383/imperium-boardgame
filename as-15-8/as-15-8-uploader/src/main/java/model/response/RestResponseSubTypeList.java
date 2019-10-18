package model.response;


import lombok.ToString;
import model.rest.SubType;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@ToString
public class RestResponseSubTypeList {
    public boolean status;
    public SubTypeResponse response;

    public Date getDate() {
        return response.Date;
    }

    public List<SubType> subTypeList(){
        return response.Records;
    }

    public Map<String, SubType> subTypeMap(){
        return response.Records.stream().collect(Collectors.toMap(SubType::getId, Function.identity()));
    }
}

@ToString
class SubTypeResponse {
    public Date Date;
    public List<SubType> Records;
}