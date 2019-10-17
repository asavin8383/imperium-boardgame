package model.traffic.projection;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class TrafficProjection {
    
    private final Long id;
    private final String name;
    private Integer count;
    private String type;

    @JsonFormat(pattern = "HH:mm dd.MM.yy")
    private Date changedDate;

    private List<String> accessToolTypes;
    private List<String> resourceTypes;

}
