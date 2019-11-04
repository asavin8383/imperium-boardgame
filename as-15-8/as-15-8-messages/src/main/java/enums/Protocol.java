package enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by san
 * Date: 04.11.2019
 */
@AllArgsConstructor
public enum Protocol {
    HTTP("http://"), HTTPS("https://");

    @Getter
    private String protocol;

    @Override
    public String toString(){
        return protocol;
    }
}
