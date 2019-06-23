package scripts.utils;

import static scripts.utils.ScriptUtils.TIME_OUT_CHECKING_ERROR;
import static scripts.utils.ScriptUtils.TIME_OUT_ERROR;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import lombok.extern.slf4j.Slf4j;
import scripts.exceptions.RobotScriptExecutionException;
import scripts.exceptions.TimeoutCheckingBrowserException;
import scripts.exceptions.TimeoutScriptException;
import scripts.utils.ScriptUtils.PageResult;


@Slf4j
public class RobotScriptUtils {

    public static final int PAGE_LOAD_TIMEOUT_SEC = 30;

    public static PageResult loadPage(String url, WebDriver webDriver) throws RobotScriptExecutionException {
        return loadPage(url, webDriver, 1, PAGE_LOAD_TIMEOUT_SEC);
    }

    public static PageResult loadPage(String url, WebDriver webDriver, int tryCount) throws RobotScriptExecutionException {
        return loadPage(url, webDriver, tryCount, PAGE_LOAD_TIMEOUT_SEC);
    }

    public static PageResult loadPage(String url,
                                      WebDriver webDriver,
                                      int tryCount,
                                      int timeoutSec) throws RobotScriptExecutionException {

        webDriver.manage().timeouts().pageLoadTimeout(timeoutSec, TimeUnit.SECONDS);

        PageResult pageSourceResult = null;
        int cnt = 0;

        while (cnt < tryCount && (pageSourceResult == null || pageSourceResult.errorCodeChrome != null)) {
            if (pageSourceResult != null) {
                ScriptUtils.waitDriver(webDriver, 3);
            }
            cnt++;

            try {
                webDriver.get(url);
                webDriver.manage().window().fullscreen();

                ScriptUtils.waitPageLoading(webDriver);
                CloudflareUtils.waitCloudflareRedirect(webDriver, timeoutSec*1000);
                ScriptUtils.waitPageLoading(webDriver);

                if (CloudflareUtils.isCloudflareError(webDriver)) {
                    pageSourceResult = new ScriptUtils.PageResult(null, CloudflareUtils.getCloudflareErrorDetailsOpt(webDriver, null));
                } else {
                    pageSourceResult = ScriptUtils.getPageSource(webDriver);
                }
            } catch (TimeoutCheckingBrowserException e) {
                log.info("TimeoutException на проверке браузера", e);
                return new ScriptUtils.PageResult(null, TIME_OUT_CHECKING_ERROR);
            } catch (TimeoutException | TimeoutScriptException e) {
                log.info("TimeoutException при получении страницы", e);
                pageSourceResult = new ScriptUtils.PageResult(null, TIME_OUT_ERROR);
            } catch (InterruptedException e) {
                throw new RobotScriptExecutionException("Выполнение потока прервано", e);
            }

            if (tryCount > 1) {
                log.info("----> Попытка загрузить страницу: {}, error: {}", cnt, pageSourceResult.errorCodeChrome);
            }
        }
        return pageSourceResult;
    }


    public static void simpleLoadPage(WebDriver driver, String url, int timeoutSec, int tryCount) {
        int cnt = 0;
        TimeoutException exception = null;

        while (++cnt <= tryCount)
            try{
                exception = null;
                driver.get(url);
                ScriptUtils.waitPageLoading(driver, timeoutSec);
                break;
            }
            catch (TimeoutException te){
                exception = te;
            }

        if (exception != null){
            throw exception;
        }
    }

    /**
     * Параллельная загрузка исходника страниц.
     **/
    public static List<PageResult> getPageSources(List<WebDriver> drivers, String url){
        List<CompletableFuture<PageResult>> pageContentFutures = drivers.stream()
                .map(webDriver -> {
                    return CompletableFuture.supplyAsync(() -> {
                        PageResult pResult = new PageResult();
                        try{
                            webDriver.get(url);
                            webDriver.manage().window().fullscreen();
                            ScriptUtils.waitDriver(webDriver, 3);
                            pResult = ScriptUtils.getPageSource(webDriver);
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                        return pResult;
                    });
                })
                .collect(Collectors.toList());

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                pageContentFutures.toArray(new CompletableFuture[0]));

        CompletableFuture<List<PageResult>> allPageContentsFuture = allFutures.thenApply(v -> {
            return pageContentFutures.stream()
                    .map(pageContentFuture -> pageContentFuture.join())
                    .collect(Collectors.toList());
        });

        List<PageResult> pageResults = allPageContentsFuture.join();
        return pageResults;
    }
}
