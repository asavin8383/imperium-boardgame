package model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DictionaryView {

    private String code;
    private int ordinal;
    private Long count;
    private String shortName;
    private String fullName;
    private LocalDateTime updateDateTime;

    public DictionaryView(String code, Long count, String shortName) {
        this.code = code;
        this.count = count;
        this.shortName = shortName;
    }

    public DictionaryView(String code, int ordinal,
                          String shortName, String fullName,
                          LocalDateTime updateDateTime) {
        this.code = code;
        this.ordinal = ordinal;
        this.shortName = shortName;
        this.fullName = fullName;
        this.updateDateTime = updateDateTime;
    }
}
