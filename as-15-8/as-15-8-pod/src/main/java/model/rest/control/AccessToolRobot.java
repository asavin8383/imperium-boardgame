package model.rest.control;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AccessToolRobot {

    private Long origId;
    private String origName;

    @Enumerated(EnumType.STRING)
    private AccessToolRobotType type;

}