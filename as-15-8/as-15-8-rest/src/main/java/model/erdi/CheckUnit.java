package model.erdi;

import checkUnits.CheckUnitType;

/**
 * Creation date: 31.05.2019
 * Author: asavin
 */
public interface CheckUnit {

    CheckUnitType getCheckUnitType();
    String getCheckUnitValue();

}
