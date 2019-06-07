package robots.impl;

import java.util.Map;

import enums.AccessToolParameters;
import enums.AccessToolUnit;
import scripts.RobotScript;
import scripts.ScriptDriverParameters;

public class SeleniumSearchRobot extends SeleniumRobot {

    private int searchResultLimit;
    private long inputDelay;

    public SeleniumSearchRobot(AccessToolUnit accessToolUnit,
    						   Class<? extends RobotScript> scriptClass,
                               ScriptDriverParameters driverParams,
                               int searchResultLimit,
                               long inputDelay) {
        super(accessToolUnit, scriptClass, driverParams);

        this.searchResultLimit = searchResultLimit;
        this.inputDelay = inputDelay;
    }

	@Override
	protected Object[] getScriptArgs(Map<AccessToolParameters, String> params) {
		return new Object[] {
			this.driverParams,
			params,
			this.searchResultLimit,
			this.inputDelay	
		};
	}
}
