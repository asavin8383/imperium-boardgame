package robots.impl;

import java.net.URL;
import java.util.Map;

import org.openqa.selenium.Platform;
import org.testng.xml.XmlTest;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import enums.AccessToolUnit;

public class SeleniumVpnRobot<T> extends SeleniumRobot<T> {


    public SeleniumVpnRobot(AccessToolUnit accessToolUnit,
                            URL hubURL,
                            Class<T> scriptClass,
                            String browserName,
                            Platform platform,
                            String applicationName) {
        super(accessToolUnit,
                hubURL,
                scriptClass,
                browserName,
                platform,
                applicationName);
    }

    @Override
    public XmlTest createTest(String name, Long jobID, CheckUnit checkUnit, Map<AccessToolParameters, String> accessToolParameters) {
        XmlTest test = super.createTest(name, jobID, checkUnit, accessToolParameters);

        test.addParameter(AccessToolParameters.STUB_URL.toString(), accessToolParameters.get(AccessToolParameters.STUB_URL));
        test.addParameter(AccessToolParameters.PROXY_DNS_NAME.toString(), accessToolParameters.get(AccessToolParameters.PROXY_DNS_NAME));
        test.addParameter(AccessToolParameters.PROXY_PORT.toString(), accessToolParameters.get(AccessToolParameters.PROXY_PORT));
        test.addParameter(AccessToolParameters.PROXY_USER.toString(), accessToolParameters.get(AccessToolParameters.PROXY_USER));
        test.addParameter(AccessToolParameters.PROXY_PASSWORD.toString(), accessToolParameters.get(AccessToolParameters.PROXY_PASSWORD));

        test.addParameter(AccessToolParameters.ETALON_PROXY_HOST.toString(), accessToolParameters.get(AccessToolParameters.ETALON_PROXY_HOST));
        test.addParameter(AccessToolParameters.ETALON_PROXY_PORT.toString(), accessToolParameters.get(AccessToolParameters.ETALON_PROXY_PORT));
        test.addParameter(AccessToolParameters.ETALON_PROXY_USERNAME.toString(), accessToolParameters.get(AccessToolParameters.ETALON_PROXY_USERNAME));
        test.addParameter(AccessToolParameters.ETALON_PROXY_PASSWORD.toString(), accessToolParameters.get(AccessToolParameters.ETALON_PROXY_PASSWORD));

        return test;
    }
}
