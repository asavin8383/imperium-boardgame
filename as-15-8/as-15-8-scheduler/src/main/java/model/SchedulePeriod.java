package model;

import lombok.Data;
import lombok.NonNull;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class SchedulePeriod {

    @NonNull
    private LocalTime startTime;
    @NonNull
    private LocalTime endTime;

    List<ArrangementProcessingPart> arrangementProcessingParts = new ArrayList<>();

    public void addArrangementProcessingPart(ArrangementProcessingPart processingPart){
        this.arrangementProcessingParts.add(processingPart);
    }
}
