package model.response;

import java.util.List;
import java.util.stream.Collectors;


public class RestResponsePASD {
    public boolean status;
    public PSADResponse response;

    public List<PSEntry> getListPSEntry(){
        return response.PASDList.stream()
                .map(entryContainer -> entryContainer.PASD)
                .collect(Collectors.toList());
    }
}

class PSADResponse {
    public List<PSADEntryContainer> PASDList;
}

class PSADEntryContainer {
    public PSEntry PASD;
}




