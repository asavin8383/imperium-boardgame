package model.response;

import java.util.List;
import java.util.stream.Collectors;


public class RestResponsePS {
    public boolean status;
    public PSResponse response;

    public List<PEntry> getListPSEntry(){
        return response.PSList.stream()
                .map(entryContainer -> entryContainer.PS)
                .collect(Collectors.toList());
    }
}

class PSResponse{
    public List<PSEntryContainer> PSList;
}

class PSEntryContainer {
    public PEntry PS;
}




