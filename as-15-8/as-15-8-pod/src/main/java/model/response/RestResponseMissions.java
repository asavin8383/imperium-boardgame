package model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import model.scheme.Mission;

import java.util.ArrayList;
import java.util.List;


public class RestResponseMissions {
    public boolean status;
    public MissionResponse response;

    public List<MissionEntry> getMissionList(){
        List<MissionEntry> list = new ArrayList<>();
        for (MissionEntryContainer m : response.MissionList){
            list.add(m.missionEntry);
        }
        return list;
    }
}

class MissionResponse {
    public List<MissionEntryContainer> MissionList;
}

class MissionEntryContainer {
    @JsonProperty("Mission")
    MissionEntry missionEntry;
}



