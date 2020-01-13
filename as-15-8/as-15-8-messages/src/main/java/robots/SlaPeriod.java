package robots;

public enum SlaPeriod {
    DAY, MONTH;
    public String propertyKey(){
        return this.name().toLowerCase();
    }
    public static SlaPeriod fromPropertyKey(String slaPeriod){
        return SlaPeriod.valueOf(slaPeriod.toUpperCase());
    }
}
