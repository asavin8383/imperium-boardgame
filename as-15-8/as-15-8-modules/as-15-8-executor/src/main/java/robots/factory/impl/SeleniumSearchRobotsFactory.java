package robots.factory.impl;

import enums.AccessToolParameters;
import enums.AccessToolUnit;
import robots.RobotDriverParameters;
import robots.impl.SeleniumRobot;

import java.util.Map;

public class SeleniumSearchRobotsFactory extends SeleniumRobotsFactory {

    private int searchResultLimit;

    public SeleniumSearchRobotsFactory(AccessToolUnit accessToolUnit,
    						   Class<? extends SeleniumRobot> scriptClass,
                               RobotDriverParameters driverParams,
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
