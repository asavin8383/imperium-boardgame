package accessTools;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by san
 * Date: 10.02.2020
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccessToolDTO {
    private String name;
    private String originalName;
    private String url;
}
