package model.enums;

import lombok.Getter;

public enum ParamSor {

    VIOLATIONS_REG_DATE( ""),
    UPDATE_CONTENT_DATE(""),
    UPDATE_ADDON_DATE(""),
    ;

    @Getter
    private String dsc;

    ParamSor(String dsc){
        this.dsc = dsc;
    }

}
