package robots.impl;

import checkUnits.CheckUnit;
import enums.AccessToolUnit;
import org.openqa.selenium.Platform;
import org.testng.xml.XmlTest;

import java.net.URL;

public class SeleniumSearchRobot<T> extends SeleniumRobot<T> {

    private String searchResultLimit;

    public SeleniumSearchRobot(AccessToolUnit accessToolUnit,
                               URL hubURL,
                               Class<T> scriptClass,
                               String browserName,
                               Platform platform,
                               String applicationName,
                               String searchResultLimit) {
        super(accessToolUnit,
                hubURL,
                scriptClass,
                browserName,
                platform,
                applicationName);

        this.searchResultLimit = searchResultLimit;
    }

    @Override
    public XmlTest createTest(String name, Long jobID, CheckUnit checkUnit) {
        XmlTest test = super.createTest(name, jobID, checkUnit);
        test.addParameter("searchResultLimit", searchResultLimit);
        return test;
    }
}
