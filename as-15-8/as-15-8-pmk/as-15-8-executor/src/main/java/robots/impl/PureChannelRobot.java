package robots.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameter;
import execution.ExecutionJobResult;
import execution.ExecutionPureChannelJobResult;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import robots.ProxyUtils;
import robots.exceptions.ExecutionException;
import robots.utils.HttpResponseHelper;
import robots.utils.RobotScriptUtils;
import robots.utils.ScriptUtils;
import robots.utils.ScriptUtils.PageResult;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static robots.utils.HttpResponseHelper.HttpResponseMeta;

@Slf4j
public class PureChannelRobot extends SeleniumRobot {

    @Getter
    @Setter
    private int restartAttempts;

    @Getter
    private final int restartInterval;

    private CompletableFuture<Void> pageGetterFuture;


    public PureChannelRobot(Map<AccessToolParameter, String> scriptParams) {

        super(scriptParams,
                ProxyUtils.getFullProxy(
                        scriptParams.get(AccessToolParameter.PROXY_TYPE),
                        scriptParams.get(AccessToolParameter.PROXY_DNS_NAME),
                        scriptParams.get(AccessToolParameter.PROXY_PORT)
                )
        );
        this.restartAttempts = Integer.parseInt(scriptParams.getOrDefault(AccessToolParameter.RESTART_ATTEMPTS, "0"));
        this.restartInterval = Integer.parseInt(scriptParams.getOrDefault(AccessToolParameter.RESTART_INTERVAL, "0"));
    }

    @Override
    public ExecutionJobResult execute(CheckUnit checkUnit, boolean throwExceptionByCaptchaOrBadIP) throws ExecutionException {
        // работайте только с хромом!
        if (!checkBrowserChrome())
            throw new ExecutionException("Ошибка, неверный браузер! Для данного робота поддерживается только браузер CHROME!");

        String url = ScriptUtils.getCheckUnitValue(checkUnit);

        ExecutionPureChannelJobResult message = new ExecutionPureChannelJobResult();

        try {
            PageResult pageResult = RobotScriptUtils.loadPage(url, driver);

            HttpResponseMeta responseMeta = HttpResponseHelper.getGetResponseMeta(driver,
                    pageResult.errorCodeChrome != null && pageResult.errorCodeChrome.toLowerCase().contains("err_"),
                    String.format("CODE: %s, checkUnit: %s, finalUrl: %s, ИСХОДНИК.",
                            pageResult.errorCodeChrome,
                            checkUnit,
                            ScriptUtils.getCurrentUrl(driver))
            );
            if (responseMeta != null) {
                message.setHttpStatus(responseMeta.status);
                message.setHttpHeaders(HttpResponseHelper.headers2Str(responseMeta.jsonHeaders));
            }

            byte[] screenShot = ScriptUtils.getScreenshot(driver);
            String finalUrl = ScriptUtils.getCurrentUrl(driver);

            message.setResponseError(pageResult.errorCodeChrome != null);
            message.setChromeErrorCode(pageResult.errorCodeChrome);
            message.setPageContent(pageResult.pageSource);
            message.setScreenshot(screenShot);
            if (pageResult.errorCodeChrome == null) {
                message.setFinalUrlPage(finalUrl);
            }
        } finally {
            close(driver);
        }

        return message;
    }

    private boolean checkBrowserChrome() {
        return "chrome".equalsIgnoreCase(getScriptParams().get(AccessToolParameter.BROWSER));
    }

    @Override
    public void destroy() throws IOException {
        if (pageGetterFuture != null)
            pageGetterFuture.cancel(true);
        super.destroy();
    }
}
