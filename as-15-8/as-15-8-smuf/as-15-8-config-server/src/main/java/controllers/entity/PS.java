package controllers.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import model.RobotType;

/**
 * User: asinjavin
 * Date: 24.10.2019
 * Time: 18:21
 */
@Data
public class PS
{
    Long id;
    String name;
    String hostname;

    @JsonIgnore
    RobotType type;
}
