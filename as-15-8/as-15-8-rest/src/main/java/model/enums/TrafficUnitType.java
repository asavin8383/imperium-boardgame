package model.enums;

public enum TrafficUnitType {

    CUSTOM,
    FORMAL,
    PHRASE,
    TEMPLATE,
    DYNAMIC

    /*
    FORMAL_ERDI("f_erdi"),
    CUSTOM_ERDI("c_erdi"),
    PHRASES("phrases"),
    TEMPLATES("templates"),
    DYNAMIC("dynamic");

    private String suffix;

    TrafficUnitType(String suffix) {
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }

    public static TrafficUnitType findBySuffix(String suffix) {
        for (TrafficUnitType value : TrafficUnitType.values()) {
            if (suffix.equals(value.getSuffix()))
                return value;
        }
        throw new IllegalArgumentException(
                "No enum constant for suffix=" + suffix);
    }
    */
}
