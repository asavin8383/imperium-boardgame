package model.traffic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import model.enums.TrafficType;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class TrafficBriefView {
    
    private final Long id;
    private final String name;
    private Long count;
    private TrafficType type;

    @JsonIgnore
    //@JsonFormat(pattern = "HH:mm dd.MM.yy")
    private Date changedDate;

    @JsonIgnore
    private List<String> accessToolTypes;

    @JsonIgnore
    private List<String> resourceTypes;

}
