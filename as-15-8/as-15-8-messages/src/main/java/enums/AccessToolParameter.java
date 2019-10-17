package enums;

/**
 * Creation date: 30.05.2019
 * Author: asavin
 * Параметры скриптов, передаваемые с фронта
 */

public enum AccessToolParameter {
    //global
    ETALON_PROXY_TYPE,
    ETALON_PROXY_HOST,
    ETALON_PROXY_PORT,
    ETALON_PROXY_USERNAME,
    ETALON_PROXY_PASSWORD,
    USE_ETALON,
    TOTAL_WORKERS_COUNT,

    //robots
    BROWSER,
    PLATFORM,
    APPLICATION,

    //search-systems
    INPUT_DELAY,
    SEARCH_RESULT_LIMIT,
    SEARCH_SYSTEM_URL,
    SEARCH_SYSTEM_RESULT_PAGE_TYPE,
    SEARCH_SYSTEM_XPATH_INPUT_FIELD,
    SEARCH_SYSTEM_XPATH_CAPTCHA,
    SEARCH_SYSTEM_XPATH_NEXT_PAGE,
    SEARCH_SYSTEM_XPATH_ITEM_LINK,

    //vpn, proxy, anonimyzers, extentions
    STUB_URL,
    PROXY_TYPE,
    PROXY_DNS_NAME,
    PROXY_PORT,
    PROXY_USER,
    PROXY_PASSWORD,
    IGNORE_CAPTCHA_APPS,
    CRX_FILE_PATH,

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
