package robots.impl;

import java.util.Map;

import enums.AccessToolParameters;
import enums.AccessToolUnit;
import scripts.RobotScript;
import scripts.ScriptDriverParameters;

public class SeleniumAnonymizerRobot extends SeleniumVpnRobot {

    private long inputDelay;

    public SeleniumAnonymizerRobot(AccessToolUnit accessToolUnit,
    							   Class<? extends RobotScript> scriptClass,
                                   ScriptDriverParameters driverParams,
                                   long inputDelay) {
        super(accessToolUnit, scriptClass, driverParams);

        this.inputDelay = inputDelay;
    }

    @Override
	protected Object[] getScriptArgs(Map<AccessToolParameters, String> params) {
		return new Object[] {
			this.driverParams,
			params,
			inputDelay
		};
	}
    
}
