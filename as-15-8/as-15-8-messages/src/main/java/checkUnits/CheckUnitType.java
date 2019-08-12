package checkUnits;

import lombok.Getter;

/**
 * Тип проверяемой единицы ЕРДИ
 * @author shabalinAI
 *
 */
public enum CheckUnitType {

	URL(CheckMethod.BROWSER),
	DOMAIN(CheckMethod.BROWSER),
	DOMAIN_MASK(CheckMethod.BROWSER),
	IP_V4(CheckMethod.BROWSER),
	IP_V6(CheckMethod.BROWSER),
	IP_V4_SUBNET(CheckMethod.NMAP),
	IP_V6_SUBNET(CheckMethod.NMAP);

	@Getter
	private CheckMethod checkMethod;

	CheckUnitType(CheckMethod checkMethod) {
		this.checkMethod = checkMethod;
	}
}
