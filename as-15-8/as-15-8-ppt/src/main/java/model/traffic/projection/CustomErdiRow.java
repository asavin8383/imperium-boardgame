package model.traffic.projection;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import model.sor.Violation;

import javax.persistence.Transient;
import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomErdiRow implements Serializable {

    public static final long serialVersionUID = 1;

    private Long customErdiId;

    private String customErdiName;

    private Violation customErdiViolation;

    private String customErdiUnitType;

    private String customErdiUnitValue;

    @Transient
    private Boolean checked;

    public CustomErdiRow(Long customErdiId,
                         String customErdiName,
                         Violation customErdiViolation,
                         String customErdiUnitType,
                         String customErdiUnitValue) {
        this.customErdiId = customErdiId;
        this.customErdiName = customErdiName;
        this.customErdiViolation = customErdiViolation;
        this.customErdiUnitType = customErdiUnitType;
        this.customErdiUnitValue = customErdiUnitValue;
    }
}
