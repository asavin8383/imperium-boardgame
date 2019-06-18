package robots.impl;

import java.util.Map;

import enums.AccessToolParameters;
import enums.AccessToolUnit;
import scripts.ScriptDriverParameters;
import scripts.impl.SeleniumRobotScript;

public class SeleniumVpnRobot extends SeleniumRobot {


    public SeleniumVpnRobot(AccessToolUnit accessToolUnit,
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
