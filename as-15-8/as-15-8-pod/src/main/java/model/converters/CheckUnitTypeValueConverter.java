package model.converters;

import checkUnits.CheckUnitType;
import model.rest.*;


public class CheckUnitTypeValueConverter {

    public static CheckUnitType convertToType(ResourceType resourceType) {
        if (resourceType == null)
            return null;

        // см класс ResourceTypeConverter - для преобразования ID в CheckUnitType

        // todo - доделать! ip_v6 - маска!

        if (resourceType instanceof TypeDomain){
            return CheckUnitType.DOMAIN;
        }
        else if (resourceType instanceof TypeDomainMask){
            return CheckUnitType.DOMAIN_MASK;
        }
        else if (resourceType instanceof TypeIp){
            return CheckUnitType.IP_V4;
        }
        else if (resourceType instanceof TypeIp6){
            return CheckUnitType.IP_V6;
        }
        else if (resourceType instanceof TypeIpSubnet){
            return CheckUnitType.IP_V4_SUBNET;
        }
        else if (resourceType instanceof TypeIp6Subnet){
            return CheckUnitType.IP_V6_SUBNET;
        }
        else if (resourceType instanceof TypeUrl){
            return CheckUnitType.URL;
        }

        return null;
    }
}
