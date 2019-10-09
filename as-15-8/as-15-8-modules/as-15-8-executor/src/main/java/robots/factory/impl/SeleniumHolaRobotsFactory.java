package robots.factory.impl;

import enums.AccessToolParameters;
import enums.AccessToolUnit;
import robots.RobotDriverParameters;
import robots.impl.SeleniumRobot;

import java.util.Map;

public class SeleniumHolaRobotsFactory extends SeleniumRobotsFactory {

	private String crxFilePath;

    public SeleniumHolaRobotsFactory(AccessToolUnit accessToolUnit,
                             Class<? extends SeleniumRobot> scriptClass,
                             RobotDriverParameters driverParams,
							 String crxFilePath) {
        super(accessToolUnit, scriptClass, driverParams);

        this.crxFilePath = crxFilePath;
    }

	@Override
	protected Object[] getScriptArgs(Map<AccessToolParameters, String> params) {
		return new Object[] {
			this.driverParams,
			params,
			this.crxFilePath
		};
	}
}
