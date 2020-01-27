package arrangement;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ArrangementToPPM {
    private Long id;
    private String title;
    private LocalDateTime creationDate;
    private LocalTime plannedStartTime;
    private LocalTime plannedEndTime;
    private Integer maxWorkersCount;
    private String accessTool;
}
