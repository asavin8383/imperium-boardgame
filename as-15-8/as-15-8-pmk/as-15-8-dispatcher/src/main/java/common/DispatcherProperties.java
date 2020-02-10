package common;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by san
 * Date: 09.02.2020
 */
@Slf4j
@Data
@ConfigurationProperties
public class DispatcherProperties {

    private final Map<String, String> accessTools = new HashMap<>();
    private ImprintProperties imprint;

    @Data
    public static class ImprintProperties{
        private boolean useImprint;
        private String header;
        private String ps;
        private String pasd;
        private String irtz;
    }
}
