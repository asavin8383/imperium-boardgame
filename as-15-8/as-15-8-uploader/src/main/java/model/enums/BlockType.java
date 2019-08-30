package model.enums;

import lombok.Getter;

public enum BlockType {

    DEFAULT("default", "блокировка по стандартным правилам"),
    DOMAIN("domain", "блокировка по доменному имени"),
    IP("ip", "блокировка по сетевому адресу"),
    DOMAIN_MASK("domain-mask", "блокировка по маске доменного имени")
    ;

    @Getter
    private String value;

    @Getter
    private String dsc;


    BlockType(String value, String dsc){
        this.value = value;
        this.dsc = dsc;
    }

    public static BlockType parse(String value){
        for (BlockType b : BlockType.values()){
            if (b.value.equalsIgnoreCase(value))
                return b;
        }
        return DEFAULT;
    }
}
