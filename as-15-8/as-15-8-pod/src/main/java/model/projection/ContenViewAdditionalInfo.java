package model.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContenViewAdditionalInfo {
    private String firstCheckUnitName;
    private Long checkUnitsCount;
    private Long erdiId;
}
