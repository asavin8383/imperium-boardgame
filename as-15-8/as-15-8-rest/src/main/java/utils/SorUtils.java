package utils;

import lombok.experimental.UtilityClass;
import model.sor.ResourceType;

import java.time.LocalDateTime;

@UtilityClass
public class SorUtils {

    public static final String BLOCK_TYPE_DEFAULT = "default";
    public static final String BLOCK_TYPE_DOMAIN = "domain";
    public static final String BLOCK_TYPE_DOMAIN_MASK = "doamin-mask";
    public static final String BLOCK_TYPE_IP = "ip";

    public static LocalDateTime getEndDate() {
        return LocalDateTime.of(3000, 1, 1, 0, 0);
    }

    public static String getResourceTypeLike(String blockType) {
        if (blockType == null || blockType.equals(BLOCK_TYPE_DEFAULT))
            return ResourceType.Description.url.toString();
        else if (blockType.contains(BLOCK_TYPE_DOMAIN))
            return ResourceType.Description.domain.toString();
        else
            return ResourceType.Description.ip.toString() + "%";
    }

}
