package service;

import checkUnits.CheckUnitType;
import enums.AccessToolUnit;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CheckUnitProcessingTimeService {

    private static Map<AccessToolUnit, Map<CheckUnitType, Integer>> checkUnitProcessingTimes = new HashMap<AccessToolUnit, Map<CheckUnitType, Integer>>(){{
        put(AccessToolUnit.SEARCH_SYSTEM, new HashMap<CheckUnitType, Integer>(){{
            put(CheckUnitType.URL, 30);
            put(CheckUnitType.IP_V4, 30);
            put(CheckUnitType.IP_V6, 30);
            put(CheckUnitType.IP_V4_SUBNET, 120);
            put(CheckUnitType.IP_V6_SUBNET, 120);
        }});
        put(AccessToolUnit.VPN, new HashMap<CheckUnitType, Integer>(){{
            put(CheckUnitType.URL, 30);
            put(CheckUnitType.IP_V4, 30);
            put(CheckUnitType.IP_V6, 30);
            put(CheckUnitType.IP_V4_SUBNET, 120);
            put(CheckUnitType.IP_V6_SUBNET, 120);
        }});
        put(AccessToolUnit.PROXY, new HashMap<CheckUnitType, Integer>(){{
            put(CheckUnitType.URL, 30);
            put(CheckUnitType.IP_V4, 30);
            put(CheckUnitType.IP_V6, 30);
            put(CheckUnitType.IP_V4_SUBNET, 120);
            put(CheckUnitType.IP_V6_SUBNET, 120);
        }});
        put(AccessToolUnit.HIDEMYASS, new HashMap<CheckUnitType, Integer>(){{
            put(CheckUnitType.URL, 30);
            put(CheckUnitType.IP_V4, 30);
            put(CheckUnitType.IP_V6, 30);
            put(CheckUnitType.IP_V4_SUBNET, 120);
            put(CheckUnitType.IP_V6_SUBNET, 120);
        }});
    }};

    public static Integer getProcessingTime(AccessToolUnit accessToolUnit, CheckUnitType checkUnitType){
        return Optional.ofNullable(checkUnitProcessingTimes.get(accessToolUnit))
                .map(accessToolMap -> Optional.ofNullable(accessToolMap.get(checkUnitType))
                        .orElse(null)
                ).orElse(null);
    }
}
