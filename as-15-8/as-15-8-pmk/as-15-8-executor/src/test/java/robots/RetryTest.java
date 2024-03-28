package robots;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import common.ApplicationConfiguration;
import enums.AccessToolParameter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import service.impl.RobotsServiceImpl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static enums.AccessToolParameter.*;
import static enums.AccessToolParameter.SEARCH_SYSTEM_XPATH_ITEM_LINK;

/**
 * Created by san
 * Date: 28.11.2023
 */
@EnableRetry
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {ApplicationConfiguration.class})
@PropertySource("file:config/application.yml")
public class RetryTest {

    @Autowired
    RobotsServiceImpl robotsService;

    public static Map<AccessToolParameter, String> googleParams() {
        Map<AccessToolParameter, String> params = new HashMap<>();
        params.put(PLATFORM, Platform.ANY.toString());
        params.put(BROWSER, "chrome");
        params.put(VERSION, "undetected_119");
        params.put(SEARCH_SYSTEM_URL, "https://www.google.ru");
        params.put(SEARCH_SYSTEM_RESULT_PAGE_TYPE, "pagination");
        params.put(SEARCH_SYSTEM_XPATH_INPUT_FIELD, "//input[@name=\"q\"]");
        params.put(SEARCH_SYSTEM_XPATH_CAPTCHA, "//form[@id=\"captcha-form\"]");
        params.put(SEARCH_SYSTEM_XPATH_NEXT_PAGE, "//*[@id=\"pnnext\"]");
        params.put(SEARCH_SYSTEM_XPATH_ITEM_LINK, "//div[@class=\"g\"]//div[@class=\"r\"]/a[1]");
        return params;
    }

    @Test
    public void execute() {

        List<String> testList = Arrays.asList(
            "sigaretioptomrf.com",
            "i.torrentfilmov.net",
            "casino-mars1.hot-tops.com",
            "godnotaba.us",
            "xhamsterhq.com",
            "chemistry-chemists.com",
            "adminchr.ru",
            "califapharmco.com",
            "epablo.biz",
            "azino777mobi.mobi",
            "rreestr.site",
            "medcpravki.co",
            "azzimut777.com",
            "porn555.com",
            "lunaseeds.com",
            "evolutionlight.ru",
            "tambov.devki.store",
            "ecolav24.biz",
            "wonderboot.click",
            "brealsglobal.com",
            "1pornodojki.com",
            "firma-diplomdx.com",
            "pornolab.biz",
            "hookahmarket.online",
            "imperium.lenin.ru",
            "jet-casino-forbizlady.one",
            "www.kurtizanki-kemerovo.com",
            "fortune-spins7.net",
            "teokrat.org",
            "onaego.com",
            "www.titancasino.com",
            "pravarfi.com",
            "premier12.ru",
            "pokerdom-ru.appspot.com",
            "постинор купит",
            "sexorenburg.ru",
            "federal-group.ru",
            "booicasino-gj.top",
            "casinofizzslots.com",
            "e-osago-to.com",
            "a.kiski38.ru",
            "hdkinoset.info",
            "sekszima.com",
            "multigaminator-777klub.com",
            "play-fortune.net",
            "prostitutkivladimiracity.com",
            "каллорий в каше",
            "1414.piratbit",
            "denuvo.piratbit",
            "bongvtv.com",
            "lineika.xyz/10-klass",
            "займы",
            "ukropen.net/wallgroups197_74630",
            "alco-shop24.ueuo.com",
            "casino-vulkan.bid/type/popular/",
            "z23.gepatit-net.com",
            "krechet-shop.ru/solaris-dark-net-ssylka.html",
            "d1x2uq5g3vui64.cloudfront.net",
            "champion-casino-zbc.buzz",
            "joycasino-bll.top",
            "joycasino-lhg.top",
            "p.mcfilm.org/mult",
            "mostbet-wu8.xyz",
            "mychampioncasinoy.xyz",
            "djoycasino.space",
            "загон для скота",
            "m.matchweek.top",
            "d.prostitutkimoskvi.org",
            "akcapital.investments",
            "kislovodsk.diplomgosznakx.com",
            "займы под залог",
            "деньги в долг",
            "baskinogo.hdrezka-1406.site",
            "деньги в займы",
            "omsk-tut.prava-online.net",
            "zfilm-hd-2734.online //",
            "juliasmith.startmail.com",
            "жизнь в долг",
            "www.donbass.ua",
            "бомбастер с сыром",
            "trade.everestfinances.co //",
            "filmitorrent.unblocked.nu",
            "jetcasinoofficial.top",
            "images.adultcomic.uk",
            "vulkan-deluxe.webcam",
            "smm-oriflame.ru",
            "sia767.myaptekas.ru",
            "su.thecinema.digital/movie/id4929665-proklyatie-artura",
            "v.25.dosug.club",
            "camwhores.ru.com",
            "tomsk.diplome-4you.com",
            "ekaterinburg.mydiplomnstore.com",
            "solcasino2109.com",
            "pushino.diplomssos.com",
            "funnyvideoonline.com",
            "dnepr.info/news",
            "103.48.190.40/video/",
            "viyoutube.co",
            "www.666.video.az",
            "diplom197.com",
            "kinovit.org"
        );

        for (String tst : testList) {
            CheckUnit checkUnit = new CheckUnit();
            checkUnit.setType(CheckUnitType.DOMAIN);
            checkUnit.setValue(tst);
            CheckUnitJob job = new CheckUnitJob();
            job.setCheckUnit(checkUnit);
            job.setAccessTool("google-local-anonimyzing");
            robotsService.run(0L, job);
        }
//
//        Map<AccessToolParameter, String> params = googleParams();
//        CommonDirectSearchRobot robot = new CommonDirectSearchRobot(params);
//
//        robot.run(checkUnit);
    }
}
