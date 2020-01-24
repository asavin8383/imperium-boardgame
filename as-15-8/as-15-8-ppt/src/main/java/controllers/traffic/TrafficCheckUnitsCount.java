package controllers.traffic;

import lombok.AllArgsConstructor;
import lombok.Data;
import model.traffic.Traffic;

@Data
@AllArgsConstructor
public class TrafficCheckUnitsCount {
    private Traffic traffic;
    private Long count;
}
