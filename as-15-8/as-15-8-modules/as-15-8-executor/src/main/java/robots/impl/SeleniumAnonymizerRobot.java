package robots.impl;

import enums.AccessToolParameters;
import enums.AccessToolUnit;
import scripts.ScriptDriverParameters;
import scripts.impl.SeleniumRobotScript;

import java.util.Map;

public class SeleniumAnonymizerRobot extends SeleniumRobot {

    public SeleniumAnonymizerRobot(AccessToolUnit accessToolUnit,
    							   Class<? extends SeleniumRobotScript> scriptClass,
                                   ScriptDriverParameters driverParams) {
        super(accessToolUnit, scriptClass, driverParams);
    }

    @Override
	protected Object[] getScriptArgs(Map<AccessToolParameters, String> params) {
		return new Object[] {
			this.driverParams,
			params
		};
	}
    
}
