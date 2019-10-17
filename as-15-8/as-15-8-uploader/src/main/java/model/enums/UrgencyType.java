package model.enums;

import lombok.Getter;

public enum UrgencyType {

    REGULAR_SPEED(0, "обычная срочность (в течение суток)"),
    HIGH_SPEED(1, "высокая срочность (незамедлительное реагирование)")
    ;

    @Getter
    private Integer value;

    @Getter
    private String dsc;


    UrgencyType(Integer value, String dsc){
        this.value = value;
        this.dsc = dsc;
    }

    public static UrgencyType parse(Integer value){
        return UrgencyType.parse(value, REGULAR_SPEED);
    }

    public static UrgencyType parse(Integer value, UrgencyType def){
        for (UrgencyType b : UrgencyType.values()){
            if (b.value.equals(value))
                return b;
        }
        return def;
    }
}
