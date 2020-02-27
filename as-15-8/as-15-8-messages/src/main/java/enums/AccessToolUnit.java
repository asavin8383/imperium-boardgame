package enums;

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

	public String propertyKey(){
		return this.name().replaceAll("_", "-").toLowerCase();
	}

	public static AccessToolUnit fromPropertyKey(String accessToolUnit){
		return AccessToolUnit.valueOf(accessToolUnit.replaceAll("-", "_").toUpperCase());
	}
}
