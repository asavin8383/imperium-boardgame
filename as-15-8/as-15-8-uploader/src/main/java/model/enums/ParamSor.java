package model.enums;

import lombok.Getter;

public enum ParamSor {

    VIOLATIONS_REG_DATE( ""),
    UPDATE_CONTENT_DATE(""),
    UPDATE_ADDON_DATE(""),
    PROCESS_CONTENT_VERSION(""),
    PROCESS_ADDON_VERSION(""),
    PROCESS_REMOVE_CONTENT_VERSION(""),
    PROCESS_REMOVE_ADDON_VERSION(""),
    ;

    @Getter
    private String dsc;

    ParamSor(String dsc){
        this.dsc = dsc;
    }

    public static ParamSor parse(String value){
        for (ParamSor v : ParamSor.values()){
            if (v.name().equalsIgnoreCase(value))
                return v;
        }
        return null;
    }

}
