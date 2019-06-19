package robots.impl;

import java.util.Map;

import enums.AccessToolParameters;
import enums.AccessToolUnit;
import scripts.RobotScript;

public class GoogleApiRobot extends AbstractRobot{

	private String searchSystemID;	
	private String key;	
	private String region;
	private int searchLimit;
	
	public GoogleApiRobot(
			AccessToolUnit accessToolUnit,
			Class<? extends RobotScript> scriptClass,
			String searchSystemID,
			String key,
			String region,
			int searchLimit) {
		
		super(accessToolUnit, scriptClass);
		this.searchSystemID = searchSystemID;
		this.key = key;
		this.region = region;
		this.searchLimit = searchLimit;
	}

	@Override
	protected Object[] getScriptArgs(Map<AccessToolParameters, String> params) {
		return new Object[] {
				this.searchSystemID,
				this.key,
				this.region,
				this.searchLimit
			};
	}

}
