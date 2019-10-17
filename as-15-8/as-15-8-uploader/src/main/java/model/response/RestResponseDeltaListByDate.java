package model.response;


import java.util.List;

public class RestResponseDeltaListByDate {
    public boolean status;
    public List<DeltaIdEntry> response;

    public List<DeltaIdEntry> getDeltaIdEntries(){
        return response;
    }
}
