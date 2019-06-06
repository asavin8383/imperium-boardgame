package robots.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import enums.AccessToolUnit;
import org.openqa.selenium.Platform;
import org.testng.xml.XmlTest;

import java.net.URL;
import java.util.Map;

public class SeleniumAnonymizerRobot<T> extends SeleniumVpnRobot<T> {

    private String inputDelay;

    public SeleniumAnonymizerRobot(AccessToolUnit accessToolUnit,
                                   URL hubURL,
                                   Class<T> scriptClass,
                                   String browserName,
                                   Platform platform,
                                   String applicationName,
                                   String inputDelay) {
        super(accessToolUnit,
                hubURL,
                scriptClass,
                browserName,
                platform,
                applicationName);

        this.inputDelay = inputDelay;
    }

    @Override
    public XmlTest createTest(String name, Long jobID, CheckUnit checkUnit,
                              Map<AccessToolParameters, String> accessToolParameters) {
        XmlTest test = super.createTest(name, jobID, checkUnit, accessToolParameters);
        test.addParameter("inputDelay", accessToolParameters.getOrDefault(
                AccessToolParameters.INPUT_DELAY, inputDelay));
        return test;
    }

}
