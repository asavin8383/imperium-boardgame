package analyzer;

import analysis.PureChannelAnalysisResult;
import checkUnits.CheckUnit;
import checkUnits.CheckUnitType;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.ApplicationConfiguration;
import execution.ExecutionPureChannelJobResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import service.impl.PureChannel_AnalyzerService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by san
 * Date: 26.02.2020
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ApplicationConfiguration.class})
@PropertySource("file:config/application.yml")
public class PureChannelAnalyzerTest {

    @Autowired
    PureChannel_AnalyzerService pureChannelAnalyzerService;

    @Test
    public void testDetailResult() {
        ExecutionPureChannelJobResult executionPureChannelJobResult = new ExecutionPureChannelJobResult();
        executionPureChannelJobResult.setAccessTool("pure-channel-pasd");
        executionPureChannelJobResult.setCheckUnit(new CheckUnit(
                null,
                CheckUnitType.URL,
                "http://myseria.net/204-temnye-nachala-serial-2020-1947.html"
        ));
        executionPureChannelJobResult.setScreenshot(new byte[10]);
        executionPureChannelJobResult.setChromeErrorCode("unknown error: net::ERR_TUNNEL_CONNECTION_FAILED\\n  (Session info: chrome\\u003d123.0.6312.58)\\nBuild info: version: \\u0027unknown\\u0027, revision: \\u0027unknown\\u0027, time: \\u0027unknown\\u0027\\nSystem info: host: \\u0027executor-cf9c678d8-ttwgf\\u0027, ip: \\u002710.244.44.111\\u0027, os.name: \\u0027Linux\\u0027, os.arch: \\u0027amd64\\u0027, os.version: \\u00275.15.0-102-generic\\u0027, java.version: \\u00271.8.0_342\\u0027\\nDriver info: org.openqa.selenium.remote.RemoteWebDriver\\nCapabilities {acceptInsecureCerts: false, browserName: chrome, browserVersion: 123.0.6312.58, chrome: {chromedriverVersion: 123.0.6312.58 (6b4b19e9dfbb..., userDataDir: /home/selenium/chrome_profile}, fedcm:accounts: true, goog:chromeOptions: {debuggerAddress: localhost:39563}, javascriptEnabled: true, networkConnectionEnabled: false, pageLoadStrategy: normal, platform: LINUX, platformName: LINUX, proxy: Proxy(manual, http\\u003das-15-8-..., setWindowRect: true, strictFileInteractability: false, timeouts: {implicit: 0, pageLoad: 300000, script: 30000}, unhandledPromptBehavior: accept, webauthn:extension:credBlob: true, webauthn:extension:largeBlob: true, webauthn:extension:minPinLength: true, webauthn:extension:prf: true, webauthn:virtualAuthenticators: true}\\nSession ID: fd7840622f5b96e0d066f59fc3bd3ab827e38e281abb5967fb9c4f1345e92110");
        executionPureChannelJobResult.setResponseError(true);
        PureChannelAnalysisResult analysisResult = (PureChannelAnalysisResult) pureChannelAnalyzerService.analyzeResult(executionPureChannelJobResult);
        System.out.println(analysisResult.getCheckResult());
    }

    @Test
    public void testCaptchaDetectionResult() throws IOException {
        String messagePath = "src/test/resources/exec_result_with_captcha.json";
        Path path = Paths.get(messagePath);
        String json = new String(Files.readAllBytes(path));
        ExecutionPureChannelJobResult executionPureChannelJobResult = new ObjectMapper().readValue(json, ExecutionPureChannelJobResult.class);
        PureChannelAnalysisResult analysisResult = (PureChannelAnalysisResult) pureChannelAnalyzerService.analyzeResult(executionPureChannelJobResult);
        System.out.println(analysisResult.getCheckResult());
    }
}
