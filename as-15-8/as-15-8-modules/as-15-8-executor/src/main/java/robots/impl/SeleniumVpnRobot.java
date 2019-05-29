package robots.impl;

import checkUnits.CheckUnit;
import enums.AccessToolUnit;
import lombok.Getter;
import org.openqa.selenium.Platform;
import org.testng.xml.XmlTest;

import java.net.URL;

public class SeleniumVpnRobot<T> extends SeleniumRobot<T> {


    @Getter
    private String etalonProxy;
    @Getter
    private Boolean useEtalonProxy = true;
    @Getter
    private Integer timeoutRequest = 30;
    @Getter
    private Integer tryCountRequest = 3;


    public SeleniumVpnRobot(AccessToolUnit accessToolUnit,
                            URL hubURL,
                            Class<T> scriptClass,
                            String browserName,
                            Platform platform,
                            String applicationName,
                            String vpnProxy,
                            String etalonProxy,
                            String useEtalonProxy,
                            String timeoutRequest,
                            String tryCountRequest) {
        super(accessToolUnit,
                hubURL,
                scriptClass,
                browserName,
                platform,
                applicationName,
                vpnProxy);

        this.etalonProxy = etalonProxy;

        this.useEtalonProxy = useEtalonProxy == null || useEtalonProxy.trim().isEmpty() ? this.useEtalonProxy : Boolean.valueOf(useEtalonProxy);
        try {
            this.timeoutRequest = Integer.valueOf(timeoutRequest);
        }
        catch (NumberFormatException e){}

        try {
            this.tryCountRequest = Integer.valueOf(tryCountRequest);
        }
        catch (NumberFormatException e){}
    }

    @Override
    public XmlTest createTest(String name, Long jobID, CheckUnit checkUnit) {
        XmlTest test = super.createTest(name, jobID, checkUnit);
        test.addParameter("etalonProxy", etalonProxy);
        test.addParameter("useEtalonProxy", useEtalonProxy.toString());
        test.addParameter("timeoutRequest", timeoutRequest.toString());
        test.addParameter("tryCountRequest", tryCountRequest.toString());
        return test;
    }
}
