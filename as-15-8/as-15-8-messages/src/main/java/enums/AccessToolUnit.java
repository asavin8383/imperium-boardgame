package enums;

import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum AccessToolUnit {

	SEARCH_SYSTEM("Поисковые системы"),
	/*GOOGLE,
	GOOGLE_API,
	YANDEX,
	MAIL,
	YAHOO,
	BING,
	SPUTNIK,
	DUCK_DUCK_GO,*/

	GOOGLE_API ("Google API"),

	VPN ("VPN"),
	/*EXPRESS,
	KASPERSKY,*/

	CAMELEO_XYZ ("Cameleo"),
	HIDEMYASS ("HideMyAss"),
	ANONYMIZER ("Анонимайзеры"),


	HOLA ("Hola"),
	EXTENSION ("Расширения для браузеров"),

	//TORGUARD,
	PROXY ("Прокси"),

	PURE_CHANNEL("Чистый канал");


	@Getter
	private String description;

	AccessToolUnit(String description) {
		this.description = description;
	}

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
