package robots.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import execution.ExecutionVpnJobResult;
import org.openqa.selenium.WebDriver;
import robots.ProxyUtils;
import robots.RobotDriverParameters;
import robots.exceptions.RobotScriptExecutionException;
import robots.utils.HttpResponseHelper;
import robots.utils.RobotScriptUtils;
import robots.utils.ScriptUtils;
import robots.utils.ScriptUtils.PageResult;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static robots.utils.HttpResponseHelper.HttpResponseMeta;


public class VPNRobot extends SeleniumRobot {

    protected boolean useEtalon;
    protected String etalonProxy;
    protected String stubUrl;

    protected volatile WebDriver etalonDriver;

    CompletableFuture<Void> pageGetterFuture;
    CompletableFuture<Void> etalonPageGetterFuture;


    public VPNRobot(RobotDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams) {

    	super(driverParams, scriptParams,
    			ProxyUtils.getFullProxy(
					scriptParams.get(AccessToolParameters.PROXY_TYPE),
					scriptParams.get(AccessToolParameters.PROXY_DNS_NAME),
					scriptParams.get(AccessToolParameters.PROXY_PORT),
					scriptParams.get(AccessToolParameters.PROXY_USER),
					scriptParams.get(AccessToolParameters.PROXY_PASSWORD)
    			)
    		);

        this.useEtalon = ScriptUtils.useEtalon(scriptParams);

    	etalonProxy = ProxyUtils.getFullProxy(
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_TYPE),
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_HOST),
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_PORT),
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_USERNAME),
    			scriptParams.get(AccessToolParameters.ETALON_PROXY_PASSWORD)
			);
     	this.stubUrl = scriptParams.get(AccessToolParameters.STUB_URL);
    }

    @Override
	public ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException {
        // работате только с хромом!
        if (!checkBrowserChrome())
            throw new RobotScriptExecutionException("Ошибка, неверный браузер! Для данного робота поддерживатся только браузер CHROME!");

        String url = ScriptUtils.getCheckUnitValue(checkUnit);

        ExecutionVpnJobResult message = new ExecutionVpnJobResult();
        message.setStubUrl(stubUrl);

        pageGetterFuture = CompletableFuture
                .runAsync(() -> {
                    try {
                        PageResult pageResult = RobotScriptUtils.loadPage(url, driver);

                        HttpResponseMeta responseMeta = HttpResponseHelper.getGetResponseMeta(driver,
                                pageResult.errorCodeChrome != null && pageResult.errorCodeChrome.toLowerCase().contains("err_"),
                                "CODE: " + pageResult.errorCodeChrome +
                                ", checkUnit: " + checkUnit.toString() +
                                ", finalUrl: " + ScriptUtils.getCurrentUrl(driver) +
                                ", ИСХОДНИК.");
                        if (responseMeta != null){
                            message.setHttpStatus(responseMeta.status);
                            message.setHttpHeaders(HttpResponseHelper.headers2Str(responseMeta.jsonHeaders));
                        }

                        byte[] screenShot = ScriptUtils.getScreenshot(driver);
                        String finalUrl = ScriptUtils.getCurrentUrl(driver);

                        message.setResponseError(pageResult.errorCodeChrome != null);
                        message.setChromeErrorCode(pageResult.errorCodeChrome);
                        message.setPageContent(pageResult.pageSource);
                        message.setScreenshot(screenShot);
                        if(pageResult.errorCodeChrome == null){
                            message.setFinalUrlPage(finalUrl);
                        }
                    }
                    catch (RobotScriptExecutionException e) {
                        throw new CompletionException(e);
                    }
                    finally {
                        close(driver);
                    }
                });

        etalonPageGetterFuture = CompletableFuture
                .runAsync(() -> {
                    if (useEtalon){
                        try {
                        	etalonDriver = createDriver(etalonProxy, true);
                            PageResult pageResult = RobotScriptUtils.loadPage(url, etalonDriver);

                            HttpResponseMeta responseMeta = HttpResponseHelper.getGetResponseMeta(etalonDriver,
                                    pageResult.errorCodeChrome != null && pageResult.errorCodeChrome.toLowerCase().contains("err_"),
                                    "CODE: " + pageResult.errorCodeChrome +
                                            ", checkUnit: " + checkUnit.toString() +
                                            ", finalUrl: " + ScriptUtils.getCurrentUrl(etalonDriver) +
                                            ", ЭТАЛОН.");
                            if (responseMeta != null){
                                message.setHttpStatusEtalon(responseMeta.status);
                                message.setHttpHeadersEtalon(HttpResponseHelper.headers2Str(responseMeta.jsonHeaders));
                            }

                            byte[] screenShot = ScriptUtils.getScreenshot(etalonDriver);
                            String finalUrl = ScriptUtils.getCurrentUrl(etalonDriver);

                            message.setChromeErrorCodeEtalon(pageResult.errorCodeChrome);
                            message.setPageContentEtalon(pageResult.pageSource);
                            message.setEtalonScreenshot(screenShot);
                            if(pageResult.errorCodeChrome == null){
                                message.setFinalUrlPageEtalon(finalUrl);
                            }
                        } catch (RobotScriptExecutionException e) {
                            throw new CompletionException(e);
                        } finally {
                            close(etalonDriver);
                        }
                    }
                });

        CompletableFuture<Void> allPageGetters = CompletableFuture.allOf(pageGetterFuture, etalonPageGetterFuture);

        try {
            allPageGetters.join();
        }
        catch(CompletionException ex) {
            try {
                throw ex.getCause();
            }
            catch(Error|RuntimeException possible) {
                throw possible;
            }
            catch(Throwable impossible) {
                throw new RobotScriptExecutionException(impossible);
            }
        } catch (CancellationException ex) {
        	throw new RobotScriptExecutionException("Робот был остановлен", ex);
        }

        message.setUseEtalon(this.useEtalon);

        return message;
    }

    private boolean checkBrowserChrome(){
        return "chrome".equalsIgnoreCase(getDriverParams().getBrowserName());
    }

    @Override
    public void destroy() throws IOException {
    	if(pageGetterFuture != null && !pageGetterFuture.isDone())
    		pageGetterFuture.cancel(true);
    	if(etalonPageGetterFuture != null && !etalonPageGetterFuture.isDone())
    		etalonPageGetterFuture.cancel(true);
    	close(etalonDriver);
    	super.destroy();
    }
}
