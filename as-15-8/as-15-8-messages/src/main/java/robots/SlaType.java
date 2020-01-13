package robots;

import checkUnits.CheckUnitType;

public enum SlaType {
    ERDI_NUMBER, TRAFFIC;

    public String propertyKey(){
        return this.name().toLowerCase();
    }
    public SlaType fromPropertyKey(String slaType){
        return SlaType.valueOf(slaType.toUpperCase());
    }
}
