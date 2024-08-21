package enums;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Creation date: 30.05.2019
 * Author: asavin
 * Параметры скриптов, передаваемые с фронта
 */
public enum AccessToolParameter {

    //	AccessToolUnit.SEARCH_SYSTEM, AccessToolUnit.GOOGLE_API, AccessToolUnit.VPN, AccessToolUnit.CAMELEO_XYZ, AccessToolUnit.HIDEMYASS, AccessToolUnit.ANONYMIZER, AccessToolUnit.HOLA, AccessToolUnit.EXTENSION, AccessToolUnit.PROXY;

    //robots
    BROWSER("Браузер",
            AccessToolUnit.SEARCH_SYSTEM,
            AccessToolUnit.VPN,
            AccessToolUnit.CAMELEO_XYZ,
            AccessToolUnit.HIDEMYASS,
            AccessToolUnit.ANONYMIZER,
            AccessToolUnit.HOLA,
            AccessToolUnit.EXTENSION,
            AccessToolUnit.PROXY,
            AccessToolUnit.PURE_CHANNEL),
    PLATFORM("Платформа",
            AccessToolUnit.SEARCH_SYSTEM,
            AccessToolUnit.VPN,
            AccessToolUnit.CAMELEO_XYZ,
            AccessToolUnit.HIDEMYASS,
            AccessToolUnit.ANONYMIZER,
            AccessToolUnit.HOLA,
            AccessToolUnit.EXTENSION,
            AccessToolUnit.PROXY,
            AccessToolUnit.PURE_CHANNEL),
    VERSION("Версия контейнера браузера",
            AccessToolUnit.SEARCH_SYSTEM,
            AccessToolUnit.VPN,
            AccessToolUnit.CAMELEO_XYZ,
            AccessToolUnit.HIDEMYASS,
            AccessToolUnit.ANONYMIZER,
            AccessToolUnit.HOLA,
            AccessToolUnit.EXTENSION,
            AccessToolUnit.PROXY,
            AccessToolUnit.PURE_CHANNEL),
    MAKE_SCREENSHOT_ON_COMPLETED("Необходимость создания скриншота для успешных проверок",
            AccessToolUnit.SEARCH_SYSTEM,
            AccessToolUnit.VPN,
            AccessToolUnit.CAMELEO_XYZ,
            AccessToolUnit.HIDEMYASS,
            AccessToolUnit.ANONYMIZER,
            AccessToolUnit.HOLA,
            AccessToolUnit.EXTENSION,
            AccessToolUnit.PROXY,
            AccessToolUnit.PURE_CHANNEL),

    //search-systems
    INPUT_DELAY("Задержка ввода поисковой строки",
            AccessToolUnit.SEARCH_SYSTEM),
    SEARCH_RESULT_LIMIT("Глубина поиска",
            AccessToolUnit.SEARCH_SYSTEM),
    SEARCH_SYSTEM_URL("URL",
            AccessToolUnit.SEARCH_SYSTEM),
    SEARCH_SYSTEM_RESULT_PAGE_TYPE("Тип вывода результатов (pagination, continuous)",
            AccessToolUnit.SEARCH_SYSTEM),
    SEARCH_SYSTEM_XPATH_INPUT_FIELD("Xpath поля ввода",
            AccessToolUnit.SEARCH_SYSTEM),
    SEARCH_SYSTEM_XPATH_NEXT_PAGE("Xpath кнопки перехода на следующую страницу",
            AccessToolUnit.SEARCH_SYSTEM),
    SEARCH_SYSTEM_XPATH_ITEM_LINK("Xpath ссылок",
            AccessToolUnit.SEARCH_SYSTEM),
    SEARCH_SYSTEM_PROXY("Прокси сервер",
            AccessToolUnit.SEARCH_SYSTEM),
    CHECK_SPELLING_LINK("Ссылка для отмены автозамены поискового запроса",
            AccessToolUnit.SEARCH_SYSTEM),
    SEARCH_SYSTEM_CHECK_HINT("Необходимость проверки нарушений в подсказке ПС",
            AccessToolUnit.SEARCH_SYSTEM),
    HINT_CLASS_NAME("CSS класс контейнера с подсказками",
            AccessToolUnit.SEARCH_SYSTEM),
    HINT_LINK_CLASS_NAME("CSS класс контейнера со ссылкой в контейнере с подсказками",
            AccessToolUnit.SEARCH_SYSTEM),
    SEARCH_SYSTEM_SEARCH_QUERY("Endpoint поискового запроса",
            AccessToolUnit.SEARCH_SYSTEM),
    SEARCH_PREFIX_FOR_URL("Префикс для формирования поискового запроса по URL",
            AccessToolUnit.SEARCH_SYSTEM),
    SEARCH_PREFIX_FOR_DOMAIN("Префикс для формирования поискового запроса по домену",
            AccessToolUnit.SEARCH_SYSTEM),

    SEARCH_SYSTEM_CAPTCHA_TYPE("Тип капчи, выдаваемый поисковой системой. Варианты (RECAPTCHA_V2)",
            AccessToolUnit.SEARCH_SYSTEM),
    SEARCH_SYSTEM_XPATH_CAPTCHA("Xpath для определения капчи",
            AccessToolUnit.SEARCH_SYSTEM),

    SEARCH_SYSTEM_XPATH_CAPTCHA_ELEMENT("Xpath элемента капчи",
            AccessToolUnit.SEARCH_SYSTEM),


    //vpn, proxy, anonymizers, extensions
    STUB_URL("Домен ПАСД",
            AccessToolUnit.VPN,
            AccessToolUnit.CAMELEO_XYZ,
            AccessToolUnit.HIDEMYASS,
            AccessToolUnit.ANONYMIZER,
            AccessToolUnit.HOLA,
            AccessToolUnit.EXTENSION,
            AccessToolUnit.PROXY,
            AccessToolUnit.PURE_CHANNEL),
    PROXY_TYPE("Протокол прокси",
            AccessToolUnit.VPN,
            AccessToolUnit.CAMELEO_XYZ,
            AccessToolUnit.HIDEMYASS,
            AccessToolUnit.ANONYMIZER,
            AccessToolUnit.HOLA,
            AccessToolUnit.EXTENSION,
            AccessToolUnit.PROXY,
            AccessToolUnit.PURE_CHANNEL),
    PROXY_DNS_NAME("Хост прокси",
            AccessToolUnit.VPN,
            AccessToolUnit.CAMELEO_XYZ,
            AccessToolUnit.HIDEMYASS,
            AccessToolUnit.ANONYMIZER,
            AccessToolUnit.HOLA,
            AccessToolUnit.EXTENSION,
            AccessToolUnit.PROXY,
            AccessToolUnit.PURE_CHANNEL),
    PROXY_PORT("Порт прокси",
            AccessToolUnit.VPN,
            AccessToolUnit.CAMELEO_XYZ,
            AccessToolUnit.HIDEMYASS,
            AccessToolUnit.ANONYMIZER,
            AccessToolUnit.HOLA,
            AccessToolUnit.EXTENSION,
            AccessToolUnit.PROXY,
            AccessToolUnit.PURE_CHANNEL),
    IGNORE_CAPTCHA_APPS("Игнорировать капчу",
            AccessToolUnit.VPN,
            AccessToolUnit.CAMELEO_XYZ,
            AccessToolUnit.HIDEMYASS,
            AccessToolUnit.ANONYMIZER,
            AccessToolUnit.HOLA,
            AccessToolUnit.EXTENSION,
            AccessToolUnit.PROXY,
            AccessToolUnit.PURE_CHANNEL),

    //universal anonymizers
    ANONYMIZER_URL("URL",
            AccessToolUnit.CAMELEO_XYZ,
            AccessToolUnit.HIDEMYASS,
            AccessToolUnit.ANONYMIZER),
    ANONYMIZER_XPATH_FIELD("Xpath поля ввода",
            AccessToolUnit.CAMELEO_XYZ,
            AccessToolUnit.HIDEMYASS,
            AccessToolUnit.ANONYMIZER),
    ANONYMIZER_XPATH_BUTTON("Xpath кнопки поиска",
            AccessToolUnit.CAMELEO_XYZ,
            AccessToolUnit.HIDEMYASS,
            AccessToolUnit.ANONYMIZER),

    //extensions
    EXTENSION_ID("ID расширения",
            AccessToolUnit.HOLA,
            AccessToolUnit.EXTENSION),
    EXTENSION_VERSION("Версия расширения",
            AccessToolUnit.HOLA,
            AccessToolUnit.EXTENSION),
    EXTENSION_POPUP("Путь к домашней странице расширения",
            AccessToolUnit.HOLA,
            AccessToolUnit.EXTENSION),
    EXTENSION_URL("URL",
            AccessToolUnit.HOLA,
            AccessToolUnit.EXTENSION),
    EXTENSION_XPATH_FIELD("Xpath поля ввода",
            AccessToolUnit.HOLA,
            AccessToolUnit.EXTENSION),
    EXTENSION_XPATH_BUTTON("Xpath кнопки поиска",
            AccessToolUnit.HOLA,
            AccessToolUnit.EXTENSION),


    //google-api
    SEARCH_SYSTEM_ID("ID клиента Google API",
            AccessToolUnit.GOOGLE_API),
    SEARCH_SYSTEM_KEY("Ключ клиента Google API",
            AccessToolUnit.GOOGLE_API),
    SEARCH_REGION("Регион поиска",
            AccessToolUnit.GOOGLE_API);

    @Getter
    @JsonIgnore
    private Set<AccessToolUnit> accessToolUnits;

    @Getter
    private String description;

    AccessToolParameter(String description, AccessToolUnit... accessToolUnits) {
        this.accessToolUnits = new HashSet<>(Arrays.asList(accessToolUnits));
        this.description = description;
    }

    public boolean hasAccessToolUnit(AccessToolUnit accessToolUnit) {
        return this.accessToolUnits.contains(accessToolUnit);
    }

    public String propertyKey() {
        return this.name().replaceAll("_", "-").toLowerCase();
    }

    public static AccessToolParameter fromPropertyKey(String accessToolParameter) {
        return AccessToolParameter.valueOf(accessToolParameter.replaceAll("-", "_").toUpperCase());
    }
}
