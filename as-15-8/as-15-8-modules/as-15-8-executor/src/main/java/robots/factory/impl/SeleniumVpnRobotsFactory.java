package robots.factory.impl;

import java.util.Map;

import enums.AccessToolParameters;
import enums.AccessToolUnit;
import robots.RobotDriverParameters;
import robots.impl.SeleniumRobot;

public class SeleniumVpnRobotsFactory extends SeleniumRobotsFactory {


    public SeleniumVpnRobotsFactory(AccessToolUnit accessToolUnit,
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
