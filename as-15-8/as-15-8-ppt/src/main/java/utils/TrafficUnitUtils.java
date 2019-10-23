package utils;

import lombok.experimental.UtilityClass;
import model.enums.TrafficUnitType;
import model.traffic.Traffic;
import model.traffic.TrafficUnit;

import java.util.Random;

@UtilityClass
public class TrafficUnitUtils {

    // "trafficId_trafficName_RandomValue_Type";

    public static final String SEPARATOR = "_";

    public static final int RANDOM_STRING_LENGTH = 8;

    // cut traffic name if overall length more than 255

    public static String getNewName(Traffic traffic, TrafficUnitType type) {
        return new StringBuilder()
                //.append(traffic.getId().toString()).append(SEPARATOR)
                .append(getTrafficPart(traffic)).append(SEPARATOR)
                .append(generateRandomStringBounded()).append(SEPARATOR)
                .append(type.name()).toString();
    }

    public static String getUpdateName(Traffic traffic, String oldName) {
        String[] arr = oldName.split(SEPARATOR);
        arr[0] = getTrafficPart(traffic);
        return String.join(SEPARATOR, arr);
    }

    private static String getTrafficPart(Traffic traffic) {
        return traffic.getName().replace(SEPARATOR, "+");
    }

    public static TrafficUnitType getType(TrafficUnit trafficUnit) {
        String[] parts = trafficUnit.getName().split(SEPARATOR);
        return TrafficUnitType.valueOf(parts[parts.length - 1]);
    }

    public static String generateRandomStringBounded() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(RANDOM_STRING_LENGTH);
        for (int i = 0; i < RANDOM_STRING_LENGTH; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }

}
