package model.traffic;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Created by san
 * Date: 21.11.2019
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DictionaryView {

    private String code;
    private Long count;
    private String shortName;

    public DictionaryView(String code, Long count, String shortName) {
        this.code = code;
        this.count = count;
        this.shortName = shortName;
    }

}
