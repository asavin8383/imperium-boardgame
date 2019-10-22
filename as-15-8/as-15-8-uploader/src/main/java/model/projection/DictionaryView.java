package model.projection;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DictionaryView {

    private String code;
    private Integer ordinal;
    private Long count;
    private String shortName;
    private String fullName;
    private Date updateDateTime;

    public DictionaryView(String code, Long count, String shortName) {
        this.code = code;
        this.count = count;
        this.shortName = shortName;
    }

    public DictionaryView(String code, int ordinal,
                          String shortName, String fullName,
                          Date updateDateTime) {
        this.code = code;
        this.ordinal = ordinal;
        this.shortName = shortName;
        this.fullName = fullName;
        this.updateDateTime = updateDateTime;
    }
}
