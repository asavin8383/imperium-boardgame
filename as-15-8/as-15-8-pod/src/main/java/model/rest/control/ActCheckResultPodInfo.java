package model.rest.control;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by san
 * Date: 21.12.2019
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActCheckResultPodInfo {
    private Long erdiId;
    private Date includeTime;
}
