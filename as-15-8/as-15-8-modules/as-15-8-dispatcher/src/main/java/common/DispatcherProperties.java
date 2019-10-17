package common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties
public class DispatcherProperties {

    private final Map<String, String> accessTools = new HashMap<>();
}
