package common;

import lombok.extern.slf4j.Slf4j;
import model.response.DeltaAddonEntry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import rest.ActCheckResult;
import rest.ActRequest;
import restapi.*;
import services.ActService;
import services.MissionService;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Configuration
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
@Slf4j
public class RestTest
{
    @Autowired
    PSRestClient psRestClient;

    @Autowired
    PASDRestClient pasdRestClient;

    @Autowired
    SubTypeRestClient subTypeRestClient;

    @Autowired
    AddonRestClient addonRestClient;

    @Autowired
    ErdiRestClient erdiRestClient;

    @Autowired
    ActService actService;

    @Autowired
    MissionService missionService;

    @Test
    public void testPS(){
        psRestClient.readFromNet();

    }

    @Test
    public void testPASD(){
        pasdRestClient.readFromNet();

    }

    @Test
    public void testSybType(){
        subTypeRestClient.readFromNet();
    }

    @Test
    public void testDeltaAddonsList(){
        List<DeltaAddonEntry> list = addonRestClient.readDeltaList();
        log.info("DeltaAddonEntry list = " + list);
    }

    @Test
    public void testAddons(){
        addonRestClient.readFullFromNet(new Date());
    }

    @Test
    public void testDeltaAddons(){
        addonRestClient.readDeltaFromNet(42, new Date());
    }

    @Test
    public void runUpdateContent(){
        //boolean res = subTypeRestClient.readFromNetDiff();
        //log.info("res = {}", res);
        //erdiRestClient.startUpdateErdi();
    }

    @Test
    public void testCreateAct(){
        ActRequest actRequest = new ActRequest();
        actRequest.setArragementId(338L);
        actRequest.setStartDate("");
        actRequest.setEndDate("");

        actService.createAct(actRequest);
    }

    @Test
    public void testNext() throws ParseException {
        Date date = erdiRestClient.getActualContentDate();
        System.out.println("Date = ");
        System.out.println(date.toString());

        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String dateStr = dateFormat.format(date);
        System.out.println(dateStr);

        // "yyyy-MM-dd'T'HH:mm:ss";
        DateFormat dateFormat2 = new SimpleDateFormat(format);
        dateFormat2.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date2 = dateFormat2.parse(dateStr);
        System.out.println("Date2 = ");
        System.out.println(date2);
        String dateStr2 = dateFormat.format(date2);
        System.out.println(dateStr2);
    }

    @Test
    public void testGetMissions(){
        missionService.fillMissionsWithConfirm(false);
    }

}
