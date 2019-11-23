package model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by san
 * Date: 22.11.2019
 */
@AllArgsConstructor
public enum SearchQueryPattern {

    ERDI("$ERDI"),
    EXPRESSION("$EXPRESSION");

    @Getter
    private String pattern;
}
