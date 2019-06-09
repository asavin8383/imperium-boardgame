package robots.impl;

import enums.AccessToolParameters;
import enums.AccessToolUnit;
import scripts.RobotScript;
import scripts.ScriptDriverParameters;

import java.util.Map;

public class SeleniumSearchRobot extends SeleniumRobot {

	// todo as db parameter
    private int searchResultLimit;

    public SeleniumSearchRobot(AccessToolUnit accessToolUnit,
    						   Class<? extends RobotScript> scriptClass,
                               ScriptDriverParameters driverParams,
                               int searchResultLimit) {
        super(accessToolUnit, scriptClass, driverParams);

        this.searchResultLimit = searchResultLimit;
    }

	@Override
	protected Object[] getScriptArgs(Map<AccessToolParameters, String> params) {
		return new Object[] {
			this.driverParams,
			params,
			this.searchResultLimit,
		};
	}
}
