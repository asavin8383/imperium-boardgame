package model.response;


import java.util.List;

public class RestResponseDeltaListByDeltaId {
    public boolean status;
    public List<DeltaIdEntry> response;

    public List<DeltaIdEntry> getDeltaIdEntries(){
        return response;
    }
}
