package enums;

/**
 * Creation date: 30.05.2019
 * Author: asavin
 * Параметры скриптов, передаваемые с фронта
 */

public enum AccessToolParameter {

    //robots
    BROWSER,
    PLATFORM,
    VERSION,

    //search-systems
    INPUT_DELAY,
    SEARCH_RESULT_LIMIT,
    SEARCH_SYSTEM_URL,
    SEARCH_SYSTEM_RESULT_PAGE_TYPE,
    SEARCH_SYSTEM_XPATH_INPUT_FIELD,
    SEARCH_SYSTEM_XPATH_CAPTCHA,
    SEARCH_SYSTEM_XPATH_NEXT_PAGE,
    SEARCH_SYSTEM_XPATH_ITEM_LINK,
    SEARCH_SYSTEM_PROXY,
    RESULT_NOT_FOUND_REGEXP,

    //vpn, proxy, anonimyzers, extentions
    STUB_URL,
    PROXY_TYPE,
    PROXY_DNS_NAME,
    PROXY_PORT,
    IGNORE_CAPTCHA_APPS,

    //extensions
    EXTENSION_ID,
    EXTENSION_VERSION,
    EXTENSION_POPUP,

    //google-api
    SEARCH_SYSTEM_ID,
    SEARCH_SYSTEM_KEY,
    SEARCH_REGION;

    public String propertyKey(){
        return this.name().replaceAll("_", "-").toLowerCase();
    }

    public static AccessToolParameter fromPropertyKey(String accessToolParameter){
        return AccessToolParameter.valueOf(accessToolParameter.replaceAll("-", "_").toUpperCase());
    }
}
