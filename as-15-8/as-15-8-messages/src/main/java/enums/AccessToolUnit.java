package enums;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum AccessToolUnit {

	SEARCH_SYSTEM,
	/*GOOGLE,
	GOOGLE_API,
	YANDEX,
	MAIL,
	YAHOO,
	BING,
	SPUTNIK,
	DUCK_DUCK_GO,*/

	GOOGLE_API,

	VPN,
	/*EXPRESS,
	KASPERSKY,*/

	CAMELEO_XYZ,
	HIDEMYASS,
	ANONYMIZER,


	HOLA,
	EXTENSION,

	//TORGUARD,
	PROXY;

	private Set<AccessToolParameter> accessToolParameters;

	public static Set<AccessToolParameter> getSetOfAccessToolParameters (AccessToolUnit accessToolUnit) {
		return Stream.of(AccessToolParameter.values())
				.filter(accessToolParameter -> accessToolParameter.hasAccessToolUnit(accessToolUnit))
				.collect(Collectors.toSet());
	}

	public String propertyKey(){
		return this.name().replaceAll("_", "-").toLowerCase();
	}

	public static AccessToolUnit fromPropertyKey(String accessToolUnit){
		return AccessToolUnit.valueOf(accessToolUnit.replaceAll("-", "_").toUpperCase());
	}
}
