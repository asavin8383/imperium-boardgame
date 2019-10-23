package model.enums;

import lombok.Getter;

public enum ParamSor {
    TEST("");

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
