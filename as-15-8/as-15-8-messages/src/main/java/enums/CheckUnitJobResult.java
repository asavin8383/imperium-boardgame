package enums;

/**
 * Результат проверки единицы мероприятия
 * @author shabalinAI
 *
 */
public enum CheckUnitJobResult {

	RUNNING,
	COMPLETED,
	FORBIDDEN_CONTENT_DETECTED,
	CAPTCHA_DETECTED,
	
	DNS_ERROR,
	SOCKET_ERROR,
	HTTP_SERVER_SEND_NO_RESPONSE,
	PAGE_NOT_FOUND,
	
	INTERNAL_ERROR,
	TIMEOUT_ERROR;
}
