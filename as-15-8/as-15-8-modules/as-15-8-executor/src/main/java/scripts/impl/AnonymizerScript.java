package scripts.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import execution.ExecutionVpnJobResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.TimeoutException;
import scripts.*;

import java.util.Map;

@Slf4j
public abstract class AnonymizerScript extends RobotScript {

    private static final String TIME_OUT_ERROR = "TIME_OUT";

    private String etalonProxy;

    private ExecutionVpnJobResult message;

    public AnonymizerScript(ScriptDriverParameters driverParams,
                            Map<AccessToolParameters, String> scriptParams) {
    	super(driverParams, scriptParams);

        this.etalonProxy = ProxyUtils.getFullProxy(
                scriptParams.get(AccessToolParameters.PROXY_TYPE),
                scriptParams.get(AccessToolParameters.ETALON_PROXY_HOST),
                scriptParams.get(AccessToolParameters.ETALON_PROXY_PORT),
                scriptParams.get(AccessToolParameters.ETALON_PROXY_USERNAME),
                scriptParams.get(AccessToolParameters.ETALON_PROXY_PASSWORD)
        );

        this.message = new ExecutionVpnJobResult();
        this.message.setStubUrl(scriptParams.get(AccessToolParameters.STUB_URL));
    }

    ExecutionJobResult process(CheckUnit checkUnit) {
        ScriptUtils.PageResult page = ScriptUtils.getPageSource(driver);

        message.setResponseError(page.errorCodeChrome != null);
        message.setChromeErrorCode(page.errorCodeChrome);
        message.setPageContent(page.pageSource);
        message.setScreenshot(ScriptUtils.getScreenshot(driver));
        if (page.errorCodeChrome == null) {
            message.setFinalUrlPage(driver.getCurrentUrl());
        }

        close(driver);

        try {
            driver = DriverFactory.createDriver(
                    getDriverParams().getHubURL(),
                    getDriverParams().getPlatformName(),
                    getDriverParams().getApplicationName(),
                    getDriverParams().getBrowserName(),
                    etalonProxy);

            driver.get(ScriptUtils.getCheckUnitValue(checkUnit));
            ScriptUtils.PageResult etalon = loadEtalon();

            message.setChromeErrorCodeEtalon(etalon.errorCodeChrome);
            message.setPageContentEtalon(etalon.pageSource);
            message.setEtalonScreenshot(ScriptUtils.getScreenshot(driver));
        } finally {
            close(driver);
        }

	    return message;
    }

    ExecutionJobResult getTimeoutMessage() {
        message.setResponseError(true);
        message.setChromeErrorCode(TIME_OUT_ERROR);
        message.setScreenshot(ScriptUtils.getScreenshot(driver));
        return message;
    }

    private ScriptUtils.PageResult loadEtalon() {
        try {
            ScriptUtils.waitPageLoading(driver);
            return ScriptUtils.getPageSource(driver);
        } catch (TimeoutException e) {
            log.info("TimeoutException при получении эталона", e);
            return new ScriptUtils.PageResult(null, TIME_OUT_ERROR);
        }
    }

}
