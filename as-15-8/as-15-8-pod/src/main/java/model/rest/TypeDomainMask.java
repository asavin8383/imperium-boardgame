package model.rest;

import lombok.ToString;


@ToString(callSuper = true)
public class TypeDomainMask extends ResourceType {

    public TypeDomainMask(String value, String ts){
        this.value = value;
        this.ts = ts;
    }
}
