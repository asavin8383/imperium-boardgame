package robots.factories.impl;

import enums.AccessToolParameters;
import enums.AccessToolUnit;
import robots.RobotDriverParameters;
import robots.impl.SeleniumRobot;

import java.util.Map;

public class SeleniumHolaRobotsFactory extends SeleniumRobotsFactory {


    public SeleniumHolaRobotsFactory(AccessToolUnit accessToolUnit,
                             Class<? extends SeleniumRobot> scriptClass,
                             RobotDriverParameters driverParams) {
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
