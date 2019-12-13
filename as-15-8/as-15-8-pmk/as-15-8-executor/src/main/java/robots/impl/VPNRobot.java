package robots.impl;

import checkUnits.CheckUnit;
import common.ExecutorProperties;
import enums.AccessToolParameter;
import execution.ExecutionJobResult;
import execution.ExecutionVpnJobResult;
import org.openqa.selenium.WebDriver;
import robots.ProxyUtils;
import robots.exceptions.ExecutionException;
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

    private boolean useEtalon;
    private String etalonProxy;
    private String stubUrl;

    private volatile WebDriver etalonDriver;

    private CompletableFuture<Void> pageGetterFuture;
    private CompletableFuture<Void> etalonPageGetterFuture;


    public VPNRobot(Map<AccessToolParameter, String> scriptParams) {

    	super(scriptParams,
    			ProxyUtils.getFullProxy(
					scriptParams.get(AccessToolParameter.PROXY_TYPE),
					scriptParams.get(AccessToolParameter.PROXY_DNS_NAME),
					scriptParams.get(AccessToolParameter.PROXY_PORT)
    			)
    		);

        ExecutorProperties.EtalonProperties etalonProperties = ExecutorProperties.getEtalon();
        this.useEtalon = etalonProperties.getEnabled();

    	etalonProxy = ProxyUtils.getFullProxy(
                etalonProperties.getProxy().getType(),
                etalonProperties.getProxy().getHost(),
                etalonProperties.getProxy().getPort()
			);
     	this.stubUrl = scriptParams.get(AccessToolParameter.STUB_URL);
    }

    @Override
	public ExecutionJobResult execute(CheckUnit checkUnit) throws ExecutionException {
        // работате только с хромом!
        if (!checkBrowserChrome())
            throw new ExecutionException("Ошибка, неверный браузер! Для данного робота поддерживатся только браузер CHROME!");

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
                    catch (ExecutionException e) {
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
                        	etalonDriver = createDriver(etalonProxy, true, checkUnit.getValue());
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
                        } catch (ExecutionException e) {
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
                throw new ExecutionException(impossible);
            }
        } catch (CancellationException ex) {
        	throw new ExecutionException("Робот был остановлен", ex);
        }

        message.setUseEtalon(this.useEtalon);

        return message;
    }

    private boolean checkBrowserChrome(){
        return "chrome".equalsIgnoreCase(getScriptParams().get(AccessToolParameter.BROWSER));
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
