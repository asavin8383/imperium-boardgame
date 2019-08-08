package model;

import lombok.Data;
import lombok.NonNull;

@Data
public class ArrangementProcessingPart {

    @NonNull
    private Arrangement arrangement;
    private int workersCount = 0;

}
